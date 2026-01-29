// src/types/vue3-sfc-loader.d.ts

// 用于声明 vue3-sfc-loader 模块的类型，解决 TypeScript 找不到模块的问题
declare module 'vue3-sfc-loader' {
  import * as Vue from 'vue';
  
  export function loadModule(url: string, options?: any): Promise<any>;
  // ... 其他你用到的方法
}