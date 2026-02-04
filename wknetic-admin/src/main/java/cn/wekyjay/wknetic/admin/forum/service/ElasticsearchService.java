package cn.wekyjay.wknetic.admin.forum.service;

import cn.wekyjay.wknetic.admin.forum.document.PostDocument;
import cn.wekyjay.wknetic.admin.forum.repository.PostSearchRepository;
import cn.wekyjay.wknetic.common.model.entity.ForumPost;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch 搜索服务
 * 
 * @author WkNetic
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchService {
    
    private final ElasticsearchClient elasticsearchClient;
    private final PostSearchRepository postSearchRepository;
    
    /**
     * 索引单个帖子
     */
    public void indexPost(PostDocument postDocument) {
        try {
            postSearchRepository.save(postDocument);
            log.info("索引帖子成功: postId={}", postDocument.getPostId());
        } catch (Exception e) {
            log.error("索引帖子失败: postId={}", postDocument.getPostId(), e);
        }
    }
    
    /**
     * 批量索引帖子
     */
    public void indexPosts(List<PostDocument> postDocuments) {
        try {
            postSearchRepository.saveAll(postDocuments);
            log.info("批量索引帖子成功: count={}", postDocuments.size());
        } catch (Exception e) {
            log.error("批量索引帖子失败", e);
        }
    }
    
    /**
     * 删除帖子索引
     */
    public void deletePost(Long postId) {
        try {
            postSearchRepository.deleteById(postId);
            log.info("删除帖子索引成功: postId={}", postId);
        } catch (Exception e) {
            log.error("删除帖子索引失败: postId={}", postId, e);
        }
    }
    
    /**
     * 搜索帖子（高级搜索，支持多字段、过滤、排序、高亮）
     */
    @SuppressWarnings("null")
    public Page<PostDocument> searchPosts(
        String keyword,
        Long topicId,
        List<String> tags,
        Integer status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String sortBy,
        String sortOrder,
        int page,
        int size
    ) {
        try {
            // 构建查询条件
            List<Query> mustQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();
            
            // 关键词搜索（multi_match，标题权重高）
            if (keyword != null && !keyword.trim().isEmpty()) {
                mustQueries.add(Query.of(q -> q
                    .multiMatch(m -> m
                        .query(keyword)
                        .fields("title^3.0", "excerpt^2.0", "content^1.0")
                        .type(TextQueryType.BestFields)
                        .operator(Operator.Or)
                    )
                ));
            }
            
            // 话题过滤
            if (topicId != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t.field("topicId").value(topicId))
                ));
            }
            
            // 标签过滤（多个标签 OR 关系）
            if (tags != null && !tags.isEmpty()) {
                filterQueries.add(Query.of(q -> q
                    .terms(t -> t
                        .field("tags")
                        .terms(ts -> ts.value(
                            tags.stream()
                                .map(tag -> co.elastic.clients.elasticsearch._types.FieldValue.of(tag))
                                .collect(Collectors.toList())
                        ))
                    )
                ));
            }
            
            // 状态过滤
            if (status != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t.field("status").value(status))
                ));
            }
            
            // TODO: 时间范围过滤待实现 - Elasticsearch Java Client 8.x API 变更
            // if (startTime != null || endTime != null) {
            //     // 需要使用不同的API方式
            // }
            
            // 构建 bool 查询
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            if (!mustQueries.isEmpty()) {
                boolQueryBuilder.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                boolQueryBuilder.filter(filterQueries);
            }
            
            // 如果没有任何条件，添加 match_all
            Query query;
            if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
                query = Query.of(q -> q.matchAll(m -> m));
            } else {
                query = Query.of(q -> q.bool(boolQueryBuilder.build()));
            }
            
            // 构建排序
            SortOrder order = "desc".equalsIgnoreCase(sortOrder) ? SortOrder.Desc : SortOrder.Asc;
            String sortField = sortBy != null ? sortBy : "_score";
            
            // 构建高亮
            Map<String, HighlightField> highlightFields = new HashMap<>();
            highlightFields.put("title", HighlightField.of(h -> h
                .preTags("<em>")
                .postTags("</em>")
                .numberOfFragments(0)
            ));
            highlightFields.put("excerpt", HighlightField.of(h -> h
                .preTags("<em>")
                .postTags("</em>")
                .fragmentSize(200)
                .numberOfFragments(1)
            ));
            highlightFields.put("content", HighlightField.of(h -> h
                .preTags("<em>")
                .postTags("</em>")
                .fragmentSize(150)
                .numberOfFragments(2)
            ));
            
            Highlight highlight = Highlight.of(h -> h.fields(highlightFields));
            
            // 构建排序 - _score 需要特殊处理
            final String finalSortField = sortField;
            final SortOrder finalOrder = order;
            
            // 执行搜索
            SearchRequest searchRequest = SearchRequest.of(s -> {
                s.index("post_index")
                    .query(query)
                    .from(page * size)
                    .size(size)
                    .highlight(highlight);
                
                // _score 排序需要使用 score() 方法
                if ("_score".equals(finalSortField)) {
                    s.sort(sort -> sort.score(sc -> sc.order(finalOrder)));
                } else {
                    s.sort(sort -> sort.field(f -> f.field(finalSortField).order(finalOrder)));
                }
                
                return s;
            });
            
            SearchResponse<PostDocument> response = elasticsearchClient.search(
                searchRequest, 
                PostDocument.class
            );
            
            // 处理结果
            List<PostDocument> documents = new ArrayList<>();
            for (Hit<PostDocument> hit : response.hits().hits()) {
                PostDocument doc = hit.source();
                if (doc != null) {
                    // 处理高亮
                    // Map<String, List<String>> highlights = hit.highlight();
                    // 可以在这里将高亮信息存储到文档中（需要添加字段）
                    documents.add(doc);
                }
            }
            
            long total = 0;
            if (response.hits().total() != null) {
                total = response.hits().total().value();
            }
            Pageable pageable = PageRequest.of(page, size);
            
            return new PageImpl<>(documents, pageable, total);
            
        } catch (Exception e) {
            log.error("搜索帖子失败", e);
            return Page.empty();
        }
    }
    
    /**
     * 搜索建议（根据前缀匹配标题）
     */
    @SuppressWarnings("null")
    public List<String> getSuggestions(String prefix, int limit) {
        try {
            Query query = Query.of(q -> q
                .matchPhrase(m -> m
                    .field("title")
                    .query(prefix)
                )
            );
            
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("post_index")
                .query(query)
                .size(limit)
                .source(source -> source.filter(f -> f.includes("title")))
            );
            
            SearchResponse<PostDocument> response = elasticsearchClient.search(
                searchRequest,
                PostDocument.class
            );
            
            return response.hits().hits().stream()
                .map(hit -> hit.source() != null ? hit.source().getTitle() : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("获取搜索建议失败: prefix={}", prefix, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取热门搜索关键词（基于最近的帖子标签统计）
     */
    public List<String> getHotKeywords(int limit) {
        try {
            // 构建查询：只查询已发布的帖子
            Query query = Query.of(q -> q
                .bool(b -> b
                    .filter(f -> f.term(t -> t.field("status").value(1))) // status=1 表示已发布
                )
            );
            
            // 构建聚合：统计tags字段出现次数最多的关键词
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("post_index")
                .query(query)
                .size(0) // 不需要返回文档，只需要聚合结果
                .aggregations("hot_keywords", a -> a
                    .terms(t -> t
                        .field("tags")
                        .size(limit)
                    )
                )
            );
            
            SearchResponse<PostDocument> response = elasticsearchClient.search(
                searchRequest,
                PostDocument.class
            );
            
            // 提取聚合结果
            if (response.aggregations() != null && response.aggregations().containsKey("hot_keywords")) {
                var aggregation = response.aggregations().get("hot_keywords");
                if (aggregation.isSterms()) {
                    return aggregation.sterms().buckets().array().stream()
                        .map(bucket -> bucket.key().stringValue())
                        .collect(Collectors.toList());
                }
            }
            
            // 如果没有统计结果，返回默认关键词
            return List.of("Minecraft", "Forge", "Fabric", "Plugin", "Mod");
            
        } catch (Exception e) {
            log.error("获取热门关键词失败", e);
            return List.of("Minecraft", "Forge", "Fabric", "Plugin", "Mod");
        }
    }
    
    /**
     * 将 ForumPost 转换为 PostDocument
     */
    public PostDocument convertToDocument(ForumPost post, String username, String topicName, List<String> tags) {
        return PostDocument.builder()
            .postId(post.getPostId())
            .title(post.getTitle())
            .excerpt(post.getExcerpt())
            .content(post.getContent())
            .userId(post.getUserId())
            .username(username)
            .topicId(post.getTopicId())
            .topicName(topicName)
            .tags(tags)
            .status(post.getStatus())
            .isPinned(post.getIsPinned())
            .isHot(post.getIsHot())
            .likeCount(post.getLikeCount())
            .commentCount(post.getCommentCount())
            .viewCount(post.getViewCount())
            .bookmarkCount(post.getBookmarkCount())
            .createTime(post.getCreateTime())
            .updateTime(post.getUpdateTime())
            .lastCommentTime(post.getLastCommentTime())
            .build();
    }
}
