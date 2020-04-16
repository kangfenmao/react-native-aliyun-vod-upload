import { NativeModules, NativeEventEmitter } from 'react-native'

const { AliyunVodFileUpload, AliyunVodVideoUpload } = NativeModules

export const AliyunVodFileUploadEmitter = new NativeEventEmitter(AliyunVodFileUpload)

export {
  AliyunVodFileUpload,
  AliyunVodVideoUpload
}
