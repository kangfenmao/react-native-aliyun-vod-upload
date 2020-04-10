import { NativeModules, NativeEventEmitter } from 'react-native'

const { AliyunVodUpload } = NativeModules

export const AliyunVodUploadEmitter = new NativeEventEmitter(AliyunVodUpload)

export default AliyunVodUpload
