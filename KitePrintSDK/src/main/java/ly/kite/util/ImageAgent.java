/*****************************************************
 *
 * ImageAgent.java
 *
 *
 * Modified MIT License
 *
 * Copyright (c) 2010-2015 Kite Tech Ltd. https://www.kite.ly
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The software MAY ONLY be used with the Kite Tech Ltd platform and MAY NOT be modified
 * to be used with any competitor platforms. This means the software MAY NOT be modified 
 * to place orders with any competitors to Kite Tech Ltd, all orders MUST go through the
 * Kite Tech Ltd platform servers. 
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 *****************************************************/

///// Package Declaration /////

package ly.kite.util;


///// Import(s) /////

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Pair;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import ly.kite.KiteSDK;


///// Class Declaration /////

/*****************************************************
 *
 * This singleton class manages (downloads, saves, and caches)
 * images.
 *
 * Images originate from a network server, and are specified
 * using a URL. Images may be stored in the cache directory on
 * the devices. This allows them to be cleared by clearing
 * the cache in the app manager.
 *
 * In-memory image caching has been removed completely. Due
 * to the stringent requirements of low-end devices, any caching
 * is best left to the caller.
 *
 *****************************************************/
public class ImageAgent
{
  ////////// Static Constant(s) //////////

  @SuppressWarnings( "unused" )
  private static final String  LOG_TAG                   = "ImageAgent";

  private static final boolean FORCE_FILE_DOWNLOAD       = false;

  private static final int     LOAD_BUFFER_SIZE_IN_BYTES = 8192;  // 8 KB


  ////////// Static Variable(s) //////////

  private static ImageAgent sImageManager;


  ////////// Member Variable(s) //////////

  private Context                  mContext;
  private File                     mCacheDirectory;

  private HashMap<String,Integer>  mURLResourceIdTable;

  private ImageLoader              mImageLoader;
  private FileDownloader           mFileDownloader;


  ////////// Static Initialiser(s) //////////


  ////////// Static Method(s) //////////

  /*****************************************************
   *
   * Returns an instance of the image manager.
   *
   *****************************************************/
  static public ImageAgent getInstance( Context context )
  {
    if ( sImageManager == null )
    {
      sImageManager = new ImageAgent( context );
    }

    return ( sImageManager );
  }


  /*****************************************************
   *
   * Converts the supplied string to a 'safe' string for
   * use in file / directory names.
   *
   *****************************************************/
  static public String toSafeString( String sourceString )
  {
    if ( sourceString == null ) return ( "" );

    int length = sourceString.length();

    char[] targetCharArray = new char[ length ];

    for ( int index = 0; index < length; index ++ )
    {
      char sourceChar = sourceString.charAt( index );

      if ( ( sourceChar >= '0' && sourceChar <= '9' ) ||
              ( sourceChar >= 'A' && sourceChar <= 'Z' ) ||
              ( sourceChar >= 'a' && sourceChar <= 'z' ) )
      {
        // Digits 0-9 and letters A-Z / a-z stay the same
        targetCharArray[ index ] = sourceChar;
      }
      else
      {
        // Everything else gets converted to underscore
        targetCharArray[ index ] = '_';
      }
    }

    return ( new String( targetCharArray ) );
  }


  /*****************************************************
   *
   * Returns a cropped bitmap image.
   *
   *****************************************************/
  static public Bitmap crop( Bitmap originalBitmap, float croppedAspectRatio )
  {
    // Get the bitmap dimensions
    int originalWidth  = originalBitmap.getWidth();
    int originalHeight = originalBitmap.getHeight();

    // Avoid divide by zero
    if ( originalHeight < KiteSDK.FLOAT_ZERO_THRESHOLD ) return ( originalBitmap );

    float originalAspectRatio = (float)originalWidth / (float)originalHeight;



    // Crop the bitmap

    Bitmap croppedBitmap;

    if ( croppedAspectRatio <= originalAspectRatio )
    {
      float croppedWidth  = originalWidth * croppedAspectRatio / originalAspectRatio;
      float croppedHeight = originalHeight;

      croppedBitmap = originalBitmap.createBitmap( originalBitmap, (int)( ( originalWidth - croppedWidth ) * 0.5f ), 0, (int)croppedWidth, (int)croppedHeight );
    }
    else
    {
      float croppedHeight = originalHeight * originalAspectRatio / croppedAspectRatio;
      float croppedWidth  = originalWidth;

      croppedBitmap = originalBitmap.createBitmap( originalBitmap, 0, (int)( ( originalHeight - croppedHeight ) * 0.5f ), (int)croppedWidth, (int)croppedHeight );
    }


    return ( croppedBitmap );
  }


  /*****************************************************
   *
   * Returns a downscaled bitmap.
   *
   * If no scaling is required, because the scaled width is
   * < 1, or the source bitmap is smaller than the scaled
   * width, then the original bitmap is returned without
   * alteration.
   *
   *****************************************************/
  static public Bitmap downscaleBitmap( Bitmap sourceBitmap, int scaledWidth )
  {
    if ( scaledWidth < 1 || sourceBitmap.getWidth() <= scaledWidth ) return ( sourceBitmap );


    // Calculate the height so as to maintain the aspect ratio

    int scaledHeight = (int)( (float)sourceBitmap.getHeight() * (float)scaledWidth / (float)sourceBitmap.getWidth() );

    return ( sourceBitmap.createScaledBitmap( sourceBitmap, scaledWidth, scaledHeight, true ) );
  }


  /*****************************************************
   *
   * Vertically flips the supplied bitmap. It is
   * flipped in place, so the bitmap must be mutable.
   *
   *****************************************************/
  static public void verticallyFlipBitmap( Bitmap bitmap )
  {
    if ( bitmap == null ) return;

    int imageWidth      = bitmap.getWidth();
    int imageHeight     = bitmap.getHeight();
    int imageHalfHeight = imageHeight >>> 1;

    int[] topRow    = new int[ imageWidth ];
    int[] bottomRow = new int[ imageWidth ];

    for ( int y = 0; y < imageHalfHeight; y ++ )
    {
      bitmap.getPixels( topRow,    0, imageWidth, 0,                   y, imageWidth, 1 );
      bitmap.getPixels( bottomRow, 0, imageWidth, 0, imageHeight - y - 1, imageWidth, 1 );

      bitmap.setPixels(bottomRow, 0, imageWidth, 0, y, imageWidth, 1);
      bitmap.setPixels( topRow,    0, imageWidth, 0, imageHeight - y - 1, imageWidth, 1 );
    }
  }


  /*****************************************************
   *
   * Rotates the supplied bitmap anticlockwise.
   *
   *****************************************************/
  static public Bitmap rotateAnticlockwiseBitmap( Bitmap sourceBitmap )
  {
    if ( sourceBitmap == null ) return ( null );


    int width  = sourceBitmap.getWidth();
    int height = sourceBitmap.getHeight();


    // Create a new Bitmap for the rotated image

    Bitmap targetBitmap = null;

    Bitmap.Config bitmapConfig = sourceBitmap.getConfig();

    try
    {
      targetBitmap = Bitmap.createBitmap( height, width, bitmapConfig );
    }
    catch ( OutOfMemoryError oome )
    {
      // If we ran out of memory trying to create a bitmap with full colour space, try
      // again using a reduced colour space.

      if ( bitmapConfig == Bitmap.Config.ARGB_8888 )
      {
        try
        {
          targetBitmap = Bitmap.createBitmap( height, width, Bitmap.Config.RGB_565 );
        }
        catch ( OutOfMemoryError oome2 )
        {
          // Give up
        }
      }
    }

    if ( targetBitmap == null ) return ( sourceBitmap );


    // Scan the source bitmap in columns

    int[] column = new int[ height ];

    for ( int x = 0; x < width; x ++ )
    {
      // Convert the column from the source to a row in the target
      sourceBitmap.getPixels( column, 0,      1, x,             0,      1, height );
      targetBitmap.setPixels( column, 0, height, 0, width - x - 1, height,      1 );
    }


    return ( targetBitmap );
  }


  ////////// Constructor(s) //////////

  private ImageAgent( Context context )
  {
    mContext            = context;
    mCacheDirectory     = context.getCacheDir();
    mURLResourceIdTable = new HashMap<>();

    mImageLoader    = ImageLoader.getInstance( context );
    mFileDownloader = FileDownloader.getInstance( context );
  }


  ////////// Method(s) //////////

  /*****************************************************
   *
   * Adds a single mapping from a URL to a resource id. This
   * is useful if we want to pre-cache any images.
   *
   *****************************************************/
  public ImageAgent addResourceMapping( String urlString, int resourceId  )
  {
    mURLResourceIdTable.put( urlString, resourceId );

    return ( this );
  }


  /*****************************************************
   *
   * Adds a set of mappings from URLs to resource ids. This
   * is useful if we want to pre-cache any images.
   *
   *****************************************************/
  public ImageAgent addResourceMappings( Pair<String,Integer>... resourceMappings )
  {
    for ( Pair<String,Integer> resourceMapping : resourceMappings )
    {
      mURLResourceIdTable.put( resourceMapping.first, resourceMapping.second );
    }

    return ( this );
  }


  /*****************************************************
   *
   * Clears any outstanding load / download requests.
   *
   * Must be called on the UI thread.
   *
   *****************************************************/
  public void clearPendingRequests()
  {
    mImageLoader.clearPendingRequests();
    mFileDownloader.clearPendingRequests();
  }


  /*****************************************************
   *
   * Returns an image directory path.
   *
   *****************************************************/
  public String getImageDirectoryPath( String imageClassString )
  {
    return ( mCacheDirectory.getPath() + File.separator + toSafeString( imageClassString ) );
  }


  /*****************************************************
   *
   * Returns an image directory path and file path.
   *
   *****************************************************/
  public Pair<String,String> getImageDirectoryAndFilePath( String imageClassString, String imageIdentifier )
  {
    // Construct the directory and file paths. The file path is: "<cache-directory>/<image-class-string>/<image-url-string>"
    // The image class string and image URL string are first converted into 'safe' strings.
    String imageDirectoryPath = getImageDirectoryPath( imageClassString );
    String imageFilePath      = imageDirectoryPath + File.separator + toSafeString( imageIdentifier );

    return ( new Pair<String,String>( imageDirectoryPath, imageFilePath ) );
  }


  /*****************************************************
   *
   * Requests an image from a file.
   *
   * Must be called on the UI thread.
   *
   *****************************************************/
  public void requestImage( Object key, File imageFile, IImageTransformer imageTransformer, int scaledImageWidth, IImageConsumer imageConsumer )
  {
    mImageLoader.requestImageLoad( key, imageFile, imageTransformer, scaledImageWidth, imageConsumer );
  }


  /*****************************************************
   *
   * Requests an image from a resource.
   *
   * Must be called on the UI thread.
   *
   *****************************************************/
  public void requestImage( Object key, int resourceId, IImageTransformer imageTransformer, int scaledImageWidth, IImageConsumer imageConsumer )
  {
    mImageLoader.requestImageLoad( key, resourceId, imageTransformer, scaledImageWidth, imageConsumer );
  }


  /*****************************************************
   *
   * Requests an image from a URI.
   *
   * Must be called on the UI thread.
   *
   *****************************************************/
  public void requestImage( Object key, Uri imageUri, IImageTransformer imageTransformer, int scaledImageWidth, IImageConsumer imageConsumer )
  {
    mImageLoader.requestImageLoad( key, imageUri, imageTransformer, scaledImageWidth, imageConsumer );
  }


  /*****************************************************
   *
   * Requests an image from a URL.
   *
   * Must be called on the UI thread.
   *
   *****************************************************/
  public void requestImage( String imageClassString, Object key, URL imageURL, IImageTransformer imageTransformer, int scaledImageWidth, IImageConsumer imageConsumer )
  {
    // First check if we have been provided with a mapping to a resource id. If so - make
    // a resource request instead.

    if ( ! FORCE_FILE_DOWNLOAD )
    {
      Integer resourceIdAsInteger = mURLResourceIdTable.get(imageURL.toString());

      if ( resourceIdAsInteger != null )
      {
        requestImage( key, resourceIdAsInteger, imageTransformer, scaledImageWidth, imageConsumer );

        return;
      }
    }

    // Next, check if it's a local asset path.
    if(imageURL.toString().contains("file:///android_asset/")) {

      Uri imageUri = Uri.parse(imageURL.toString());
      mImageLoader.requestImageLoad( key, imageUri, imageTransformer, scaledImageWidth, imageConsumer );
      return;
    }




    // Generate the directory and file that the image would be downloaded to

    Pair<String, String> directoryAndFilePath = getImageDirectoryAndFilePath( imageClassString, imageURL.toString() );

    String imageDirectoryPath = directoryAndFilePath.first;
    String imageFilePath      = directoryAndFilePath.second;


    // See if we already have the image in cache

    File imageDirectory = new File( imageDirectoryPath );
    File imageFile      = new File( imageFilePath );

    if ( ( ! FORCE_FILE_DOWNLOAD ) && imageFile.exists() )
    {
      // Make a request to load the image

      mImageLoader.requestImageLoad( key, imageFile, imageTransformer, scaledImageWidth, imageConsumer );
    }
    else
    {
      // Notify the consumer that the image will need to be downloaded
      imageConsumer.onImageDownloading( key );


      // Make a request to download the image, but use an intermediate callback which then makes
      // a request to load the image following the download.

      DownloadCallback downloadCallback = new DownloadCallback( key, imageTransformer, scaledImageWidth, imageConsumer );

      mFileDownloader.requestFileDownload( imageURL, imageDirectory, imageFile, downloadCallback );
    }
  }


  /*****************************************************
   *
   * Requests an image from a remote URL. Must be called
   * on the UI thread.
   *
   *****************************************************/
  public void requestImage( String imageClassString, Object key, URL imageURL, IImageConsumer imageConsumer )
  {
    requestImage( imageClassString, key, imageURL, null, 0, imageConsumer );
  }


  /*****************************************************
   *
   * Requests an image from a remote URL. Must be called
   * on the UI thread.
   *
   *****************************************************/
  public void requestImage( String imageClassString, URL imageURL, IImageConsumer imageConsumer )
  {
    requestImage( imageClassString, imageURL, imageURL, null, 0, imageConsumer );
  }


  /*****************************************************
   *
   * Requests an image from an existing bitmap.
   *
   * Must be called on the UI thread.
   *
   *****************************************************/
  public void requestImage( Object key, Bitmap bitmap, IImageTransformer imageTransformer, int scaledImageWidth, IImageConsumer imageConsumer )
  {
    mImageLoader.requestImageLoad( key, bitmap, imageTransformer, scaledImageWidth, imageConsumer );
  }


  /*****************************************************
   *
   * Requests an image from image data.
   *
   * Must be called on the UI thread.
   *
   *****************************************************/
  public void requestImage( Object key, byte[] imageBytes, IImageTransformer imageTransformer, int scaledImageWidth, IImageConsumer imageConsumer )
  {
    mImageLoader.requestImageLoad( key, imageBytes, imageTransformer, scaledImageWidth, imageConsumer );
  }


  ////////// Inner Class(es) //////////

  /*****************************************************
   *
   * A download callback that makes a load request.
   *
   *****************************************************/
  private class DownloadCallback implements FileDownloader.ICallback
  {
    private Object             mKey;
    private IImageTransformer  mImageTransformer;
    private int                mScaledImageWidth;
    private IImageConsumer     mImageConsumer;


    DownloadCallback( Object key, IImageTransformer imageTransformer, int scaledImageWidth, IImageConsumer imageConsumer )
    {
      mKey              = key;
      mImageTransformer = imageTransformer;
      mScaledImageWidth = scaledImageWidth;
      mImageConsumer    = imageConsumer;
    }


    @Override
    public void onDownloadSuccess( URL sourceURL, File targetDirectory, File targetFile )
    {
      // Once the image has downloaded - immediately request that it be loaded
      mImageLoader.requestImageLoad( mKey, targetFile, mImageTransformer, mScaledImageWidth, mImageConsumer );
    }

    @Override
    public void onDownloadFailure( URL sourceURL, Exception exception )
    {
      mImageConsumer.onImageUnavailable( mKey, exception );
    }

  }

}
