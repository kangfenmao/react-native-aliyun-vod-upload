import { DeviceEventEmitterStatic } from 'react-native';

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

export function init(
  init: InitParams,
  callback: (result: any) => void
): void

export function addFile(file: File): void
export function deleteFile(index: number): void
export function clearFiles(): void
export function listFiles(callback: (files: File[]) => void): void
export function cancelFile(index: number): void
export function start(): void
export function stop(): void
export function pause(): void
export function resume(): void

export const AliyunVodUploadEmitter: DeviceEventEmitterStatic
