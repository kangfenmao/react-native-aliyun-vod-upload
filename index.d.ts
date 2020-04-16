import { DeviceEventEmitterStatic } from 'react-native'

interface File {
  path: string
  type: string
  title: string
  desc: string
  cateId: number
  tags: string
}

interface InitParams {
  videoId: string
  uploadAuth: string
  uploadAddress: string
}

interface AliyunVodFileUploadInterface {
  init(init: InitParams, callback: (result: any) => void): void
  addFile(file: File): void
  deleteFile(index: number): void
  clearFiles(): void
  listFiles(callback: (files: File[]) => void): void
  cancelFile(index: number): void
  start(): void
  stop(): void
  pause(): void
  resume(): void
}

export const AliyunVodFileUpload: AliyunVodFileUploadInterface
export const AliyunVodFileUploadEmitter: DeviceEventEmitterStatic
export const AliyunVodVideoUploadEmitter: DeviceEventEmitterStatic
