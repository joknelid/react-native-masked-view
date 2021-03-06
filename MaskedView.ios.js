/**
 * @providesModule MaskedView
 * @flow
 */

'use strict';

var React = require('react');
var {
  PropTypes
} = React;
var ReactNative = require('react-native');
var {
  requireNativeComponent,
  View
} = ReactNative;

var resolveAssetSource = require('../react-native/Libraries/Image/resolveAssetSource');

var MaskedView = React.createClass({
  propTypes: {
    ...View.propTypes,

    /**
     * `uri` is a string representing the resource identifier for the image, which
     * could be an http address, a local file path, or the name of a static image
     * resource (which should be wrapped in the `require('image!name')` function).
     */
    maskImage: PropTypes.oneOfType([
      PropTypes.shape({
        uri: PropTypes.string,
      }),
      // Opaque type returned by require('./image.jpg')
      PropTypes.number,
    ]),
  },

  render() {
    var imgSource = resolveAssetSource(this.props.maskImage);
    return (
      <NativeMaskedView
        {...this.props}
        maskImage={imgSource}
      />
    );
  },

});

var NativeMaskedView = requireNativeComponent('RNMaskedView', MaskedView);

module.exports = MaskedView;
