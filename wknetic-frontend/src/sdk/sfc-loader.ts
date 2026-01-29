import * as Vue from 'vue';
import { loadModule } from 'vue3-sfc-loader';

export const loadVueComponent = async (url: string) => {
  const options = {
    moduleCache: { vue: Vue },
    async getFile(url: string) {
      const res = await fetch(url);
      if (!res.ok) throw Object.assign(new Error(res.statusText + ' ' + url), { res });
      return {
        getContentData: (asBinary: boolean) => asBinary ? res.arrayBuffer() : res.text(),
      }
    },
    addStyle(textContent: string) {
      const style = document.createElement('style');
      style.textContent = textContent;
      const ref = document.head.getElementsByTagName('style')[0] || null;
      document.head.insertBefore(style, ref);
    },
  };
  return await loadModule(url, options);
};