package cn.wekyjay.wknetic.admin.forum.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 配置类
 * 
 * @author WkNetic
 * @since 2026-02-02
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "cn.wekyjay.wknetic.admin.forum.repository")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    /**
     * 配置 RestClient
     */
    @Bean
    public RestClient restClient() {
        // 解析 URI
        String host = "localhost";
        int port = 9200;
        String scheme = "http";
        
        if (elasticsearchUri != null && !elasticsearchUri.isEmpty()) {
            elasticsearchUri = elasticsearchUri.replace("http://", "").replace("https://", "");
            if (elasticsearchUri.startsWith("https")) {
                scheme = "https";
            }
            String[] parts = elasticsearchUri.split(":");
            if (parts.length > 0) {
                host = parts[0];
            }
            if (parts.length > 1) {
                port = Integer.parseInt(parts[1]);
            }
        }
        
        return RestClient.builder(new HttpHost(host, port, scheme)).build();
    }

    /**
     * 配置 ElasticsearchClient
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        RestClientTransport transport = new RestClientTransport(
            restClient, 
            new JacksonJsonpMapper()
        );
        return new ElasticsearchClient(transport);
    }
}
