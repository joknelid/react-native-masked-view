//
//  RNMaskedView.m
//  RNMaskedView
//

#import "RNMaskedView.h"

#import "RCTConvert.h"
#import "RCTBridge.h"
#import "RCTImageLoader.h"
#import "RCTUIManager.h"

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>

@implementation RNMaskedView {
  UIImage *_maskUIImage;
  RCTBridge *_bridge;
  CALayer *_mask;
}

- (instancetype)initWithBridge:(RCTBridge *)bridge
{
  if ((self = [super init])) {
    _bridge = bridge;
    _maskUIImage = nil;
    _mask = nil;
  }
  return self;
}

- (void)layoutSubviews
{
  [super layoutSubviews];

  if (_maskUIImage != nil) {
    if (_mask == nil) {
      _mask = [CALayer layer];
    }
    _mask.contents = (id)[_maskUIImage CGImage];
    _mask.frame = self.bounds; //TODO custom: CGRectMake(left, top, width, height););
    self.layer.mask = _mask;
    self.layer.masksToBounds = YES;
  }

}

- (void)setMaskImage:(NSDictionary *)source
{
  NSLog(@"MaskedView - setMaskedImage source: %@", source);
    
  if (![source isEqual:_maskImage]) {
    _maskImage = [source copy];
    NSString *imageTag = [RCTConvert NSString:_maskImage[@"uri"]];
    CGFloat scale = [RCTConvert CGFloat:_maskImage[@"scale"]] ?: 1;
    
    NSLog(@"MaskedView - setMaskedImage loading image tag:%@ scale: %d", imageTag, scale);
      
    __weak RNMaskedView *weakSelf = self;
    [_bridge.imageLoader loadImageWithTag:imageTag
                                     size:CGSizeZero
                                    scale:scale
                               resizeMode:UIViewContentModeScaleToFill
                            progressBlock:nil
                          completionBlock:^(NSError *error, UIImage *image) {

                            dispatch_async(dispatch_get_main_queue(), ^{
                              NSLog(@"MaskedView - async block start, weakSelf: %p", weakSelf);
                              if (weakSelf) {
                                RNMaskedView *strongSelf = weakSelf;
                                strongSelf->_maskUIImage = image;
                                [strongSelf setNeedsLayout];
                              }
                              NSLog(@"MaskedView - async block end");
                            });
                          }];
  }
  
}

- (void)displayLayer:(CALayer *)layer
{
//    override displayLayer because the build-in RCTView
//    #displayLayer kills the mask
}

@end
