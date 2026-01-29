import type { WknieticSDK } from '@/sdk'

declare global {
  interface Window {
    WknieticSDK: typeof WknieticSDK
  }
}

export {}