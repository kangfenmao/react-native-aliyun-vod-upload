#import "AliyunVodFileUpload.h"
#import <React/RCTConvert.h>

@implementation AliyunVodFileUpload

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents
{
  return @[
      @"onUploadSucceed",
      @"OnUploadFailed",
      @"OnUploadProgress",
      @"OnUploadTokenExpired",
      @"OnUploadRerty",
      @"OnUploadRertyResume",
      @"OnUploadStarted",
  ];
}

RCT_EXPORT_METHOD(init:(NSDictionary *)config callback:(RCTResponseSenderBlock)callback)
{
    NSString *uploadAuth = [RCTConvert NSString:config[@"uploadAuth"]];
    NSString *uploadAddress = [RCTConvert NSString:config[@"uploadAddress"]];
    self.videoId = [RCTConvert NSString:config[@"videoId"]];

    // create VODUploadClient object
    self.uploader = [VODUploadClient new];

    // weakself
    __weak typeof(self) weakSelf = self;

    // setup callback
    OnUploadFinishedListener FinishCallbackFunc = ^(UploadFileInfo* fileInfo, VodUploadResult* result){
        NSLog(@"upload finished callback videoid: %@", self.videoId);
        [self sendEventWithName:@"onUploadSucceed" body:@{
            @"videoId": self.videoId,
            @"bucket": result.bucket,
            @"endpoint": result.endpoint
        }];
    };

    OnUploadFailedListener FailedCallbackFunc = ^(UploadFileInfo* fileInfo, NSString *code, NSString* message){
        NSLog(@"upload failed callback code = %@, error message = %@", code, message);
        [self sendEventWithName:@"OnUploadFailed" body:@{
            @"code": code,
            @"message": message
        }];
    };

    OnUploadProgressListener ProgressCallbackFunc = ^(UploadFileInfo* fileInfo, long uploadedSize, long totalSize) {
        NSLog(@"upload progress callback uploadedSize : %li, totalSize : %li", uploadedSize, totalSize);
        [self sendEventWithName:@"OnUploadProgress" body:@{
            @"uploadedSize": @(uploadedSize),
            @"totalSize": @(totalSize),
            @"progress": @(uploadedSize * 1.000000 / totalSize)
        }];
    };

    OnUploadTokenExpiredListener TokenExpiredCallbackFunc = ^{
        NSLog(@"upload token expired callback.");
        // token过期，设置新的上传凭证，继续上传
        [weakSelf.uploader resumeWithAuth:@""];
        [self sendEventWithName:@"OnUploadTokenExpired" body:@{
            @"message": @"upload token expired."
        }];
    };

    OnUploadRertyListener RetryCallbackFunc = ^{
        NSLog(@"upload retry begin callback.");
        [self sendEventWithName:@"OnUploadRerty" body:@{
            @"message": @"upload retry begin."
        }];
    };

    OnUploadRertyResumeListener RetryResumeCallbackFunc = ^{
        NSLog(@"upload retry end callback.");
        [self sendEventWithName:@"OnUploadRertyResume" body:@{
            @"message": @"upload retry resume."
        }];
    };

    OnUploadStartedListener UploadStartedCallbackFunc = ^(UploadFileInfo* fileInfo) {
        NSLog(@"upload upload started callback.");
        // 设置上传地址 和 上传凭证
        [weakSelf.uploader setUploadAuthAndAddress:fileInfo uploadAuth:uploadAuth uploadAddress:uploadAddress];
        [self sendEventWithName:@"OnUploadStarted" body:@{
            @"message": @"upload upload started."
        }];
    };

    VODUploadListener *listener = [[VODUploadListener alloc] init];

    listener.finish = FinishCallbackFunc;
    listener.failure = FailedCallbackFunc;
    listener.progress = ProgressCallbackFunc;
    listener.expire = TokenExpiredCallbackFunc;
    listener.retry = RetryCallbackFunc;
    listener.retryResume = RetryResumeCallbackFunc;
    listener.started = UploadStartedCallbackFunc;

    // init with upload address and upload auth
    [self.uploader setListener:listener];

    NSLog(@"init uploadAuth = %@, uploadAddress = %@", uploadAuth, uploadAddress);
    callback(@[@"[VodUploadClient] init success."]);
}

RCT_EXPORT_METHOD(addFile:(NSDictionary *)file)
{
    NSString *filePath = [RCTConvert NSString:file[@"path"]];
    NSString *type = [RCTConvert NSString:file[@"type"]];
    NSString *title = [RCTConvert NSString:file[@"title"]];
    NSString *desc = [RCTConvert NSString:file[@"desc"]];
    NSNumber *cateId = [RCTConvert NSNumber:file[@"cateId"]];
    NSString *tags = [RCTConvert NSString:file[@"tags"]];

    VodInfo *vodInfo = [[VodInfo alloc] init];
    vodInfo.title = title;
    vodInfo.desc = desc;
    vodInfo.cateId = cateId;
    vodInfo.tags = tags;

    Boolean added = [self.uploader addFile:filePath vodInfo:vodInfo];

    if (added) {
        NSLog(@"addFile path = %@, type = %@", filePath, type);
    } else {
        NSLog(@"addFile failure");
    }
}

/* 管理上传队列 */

/**
 * 从队列中删除上传文件
 * 如果待删除的文件正在上传中，则取消上传并自动上传下一个文件
 */
RCT_EXPORT_METHOD(deleteFile:(int)index)
{
    [self.uploader deleteFile:index];
}

/**
 * 清空上传队列
 * 如果有文件在上传，则取消上传
 */
RCT_EXPORT_METHOD(clearFiles)
{
    [self.uploader clearFiles];
}

/**
 * 获取上传文件队列
 */
RCT_EXPORT_METHOD(listFiles:(RCTResponseSenderBlock)callback)
{
   NSMutableArray *files = [self.uploader listFiles];
   NSLog(@"listFiles %@", files);

   callback(files);
}

/**
 * 将文件标记为取消
 * 文件任保留在上传列表中。如果待取消的文件正在上传中，则取消上传并自动上传下一个文件
 */
RCT_EXPORT_METHOD(cancelFile:(int)index)
{
    [self.uploader cancelFile:index];
}


/* 上传控制 */

/**
 * 开始上传
 */
RCT_EXPORT_METHOD(start)
{
    [self.uploader start];
}

/**
 * 停止上传
 * 如果有文件正在上传中，则取消上传
 */
RCT_EXPORT_METHOD(stop)
{
    [self.uploader stop];
}

/**
 * 恢复待上传文件
 * 或者清空队列后重新添加文件上传
 */
RCT_EXPORT_METHOD(pause)
{
    [self.uploader pause];
}

/**
 * 恢复上传
 */
RCT_EXPORT_METHOD(resume)
{
    [self.uploader resume];
}

@end
