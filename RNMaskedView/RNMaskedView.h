//
//  RNMaskedView.h
//  RNMaskedView
//

#import <UIKit/UIKit.h>
#import "RCTView.h"

@class RCTBridge;

@interface RNMaskedView : RCTView

- (instancetype)initWithBridge:(RCTBridge *)bridge;

@property (nonatomic, copy) NSDictionary *maskImage;

@end
