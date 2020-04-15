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
import AliyunVodUpload from 'react-native-aliyun-vod-upload'

// Init
AliyunVodUpload.init(
  {
    videoId: '',
    uploadAuth: '',
    uploadAddress: ''
  },
  result => {
    console.log(result)
  }
)

AliyunVodUpload.addFile({
  path: '',
  type: 'mp4',
  title: '',
  desc: '',
  cateId: 1,
  tags: ''
})

AliyunVodUpload.start()
```

## API

```javascript
// Manage files
AliyunVodUpload.deleteFile(index: number)
AliyunVodUpload.clearFiles()
AliyunVodUpload.listFiles(callback: (files: File[]) => void)
AliyunVodUpload.cancelFile(index: number)

// Upload control
AliyunVodUpload.start()
AliyunVodUpload.stop()
AliyunVodUpload.pause()
AliyunVodUpload.resume()
```

## Event Listener

```javascript
emitters: EmitterSubscription[] = []

componentDidMount() {
  this.emitters.push(
    AliyunVodUploadEmitter.addListener('OnUploadStarted', (result: any) => {
      console.log('[start]', result)
    }),
    AliyunVodUploadEmitter.addListener('OnUploadProgress', (result: any) => {
      console.log('[progress]', Math.floor(result.progress * 100) + '%')
    }),
    AliyunVodUploadEmitter.addListener('OnUploadFailed', (result: any) => {
      console.log('[failed]', result)
    }),
    AliyunVodUploadEmitter.addListener('onUploadSucceed', (result: any) => {
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