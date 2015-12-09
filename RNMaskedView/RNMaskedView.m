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
}

- (instancetype)initWithBridge:(RCTBridge *)bridge
{
  if ((self = [super init])) {
    _bridge = bridge;
  }
  return self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    
    CALayer *mask = [CALayer layer];
    mask.contents = (id)[_maskUIImage CGImage];
    mask.frame = self.bounds; //TODO custom: CGRectMake(left, top, width, height);
    self.layer.mask = mask;
    self.layer.masksToBounds = YES;
}

- (void)setMaskImage:(NSDictionary *)source
{
  if (![source isEqual:_maskImage]) {
    _maskImage = [source copy];
    NSString *imageTag = [RCTConvert NSString:_maskImage[@"uri"]];
    CGFloat scale = [RCTConvert CGFloat:_maskImage[@"scale"]] ?: 1;
    
    __weak RNMaskedView *weakSelf = self;
    [_bridge.imageLoader loadImageWithTag:imageTag
                                     size:CGSizeZero
                                    scale:scale
                               resizeMode:UIViewContentModeScaleToFill
                            progressBlock:nil
                          completionBlock:^(NSError *error, UIImage *image) {

                            dispatch_async(_bridge.uiManager.methodQueue, ^{
                              RNMaskedView *strongSelf = weakSelf;
                              strongSelf->_maskUIImage = image;
                              [strongSelf setNeedsLayout];
                              [strongSelf setNeedsDisplay];
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
