# react-native-aliyun-vod-upload

## Getting started

`$ npm install react-native-aliyun-vod-upload`

### iOS

`cd ios && pod install && cd ..`

### Android

Add aliyun repo to `android/build.gradle`

```gradle
repositories {
    // ...
    maven {
      url "https://maven.aliyun.com/nexus/content/repositories/releases"
    }
}
```

## Usage

```javascript
import { AliyunVodFileUpload, AliyunVodFileUploadEmitter } from 'react-native-aliyun-vod-upload'

// Init
AliyunVodFileUpload.init(
  {
    videoId: '',
    uploadAuth: '',
    uploadAddress: ''
  },
  result => {
    console.log(result)
  }
)

AliyunVodFileUpload.addFile({
  path: '',
  type: 'mp4',
  title: '',
  desc: '',
  cateId: 1,
  tags: ''
})

AliyunVodFileUpload.start()
```

## API

```javascript
// Manage files
AliyunVodFileUpload.deleteFile(index: number)
AliyunVodFileUpload.clearFiles()
AliyunVodFileUpload.listFiles(callback: (files: File[]) => void)
AliyunVodFileUpload.cancelFile(index: number)

// Upload control
AliyunVodFileUpload.start()
AliyunVodFileUpload.stop()
AliyunVodFileUpload.pause()
AliyunVodFileUpload.resume()
```

## Event Listener

```javascript
emitters: EmitterSubscription[] = []

componentDidMount() {
  this.emitters.push(
    AliyunVodFileUploadEmitter.addListener('OnUploadStarted', (result: any) => {
      console.log('[start]', result)
    }),
    AliyunVodFileUploadEmitter.addListener('OnUploadProgress', (result: any) => {
      console.log('[progress]', Math.floor(result.progress * 100) + '%')
    }),
    AliyunVodFileUploadEmitter.addListener('OnUploadFailed', (result: any) => {
      console.log('[failed]', result)
    }),
    AliyunVodFileUploadEmitter.addListener('onUploadSucceed', (result: any) => {
      console.log('[succeed]', result)
    })
  )
}

componentWillUnmount() {
  this.emitters.forEach(e => e.remove())
}
```

### All Event

```
onUploadSucceed
OnUploadFailed
OnUploadProgress
OnUploadTokenExpired
OnUploadRerty
OnUploadRertyResume
OnUploadStarted
```