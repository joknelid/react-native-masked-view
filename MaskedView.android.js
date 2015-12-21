/**
 * @providesModule MaskedView
 * @flow
 */

'use strict';

var React = require('react-native');
var {
  requireNativeComponent,
  PropTypes,
} = React;

var resolveAssetSource = require('../react-native/Libraries/Image/resolveAssetSource');

var MaskedView = React.createClass({
  propTypes: {
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
        maskImage={imgSource.uri}
      />
    );
  },

});

var NativeMaskedView = requireNativeComponent('RNMaskedView', null);

module.exports = MaskedView;
