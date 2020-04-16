#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <VODUpload/VODUploadClient.h>

@interface AliyunVodFileUpload : RCTEventEmitter <RCTBridgeModule>

@property (nonatomic, strong) VODUploadClient *uploader;
@property NSString* videoId;

@end
