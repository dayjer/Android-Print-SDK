/*****************************************************
 *
 * AAREImageContainerFrame.java
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 *****************************************************/

///// Package Declaration /////

package ly.kite.widget;


///// Import(s) /////

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.net.URL;

import ly.kite.KiteSDK;
import ly.kite.R;
import ly.kite.catalogue.Asset;
import ly.kite.catalogue.AssetHelper;
import ly.kite.util.IImageConsumer;
import ly.kite.util.ImageAgent;


///// Class Declaration /////

/*****************************************************
 *
 * This widget is a frame layout that contains an image view.
 * The size of the image is set according to the aspect ratio
 * and the width of the frame.
 *
 * The widget is also an image consumer.
 *
 *****************************************************/
abstract public class AAREImageContainerFrame extends FrameLayout implements IImageConsumer, Animation.AnimationListener
  {
  ////////// Static Constant(s) //////////

  @SuppressWarnings( "unused" )
  static private final String  LOG_TAG                           = "AAREImageContainerFrame";

  static private final boolean DEBUGGING_IS_ENABLED              = false;

  static private final Object  ANY_KEY                           = new Object();

  static private final long    FADE_IN_ANIMATION_DURATION_MILLIS = 300L;
  static private final float   ALPHA_TRANSPARENT                 = 0.0f;
  static private final float   ALPHA_OPAQUE                      = 1.0f;


  ////////// Static Variable(s) //////////


  ////////// Member Variable(s) //////////

  private   int                  mWidth;
  private   int                  mHeight;

  protected ImageView            mImageView;
  protected ProgressBar          mProgressSpinner;

  private   AspectRatioEnforcer  mAspectRatioEnforcer;

  private   boolean              mShowProgressSpinnerOnDownload;

  private   String               mRequestImageClass;
  private   Object               mRequestImageSource;

  private   Object               mExpectedKey;

  protected Animation            mFadeInAnimation;


  ////////// Static Initialiser(s) //////////


  ////////// Static Method(s) //////////


  ////////// Constructor(s) //////////

  public AAREImageContainerFrame( Context context )
    {
    super( context );

    initialise( context, null, 0 );
    }

  public AAREImageContainerFrame( Context context, AttributeSet attrs )
    {
    super( context, attrs );

    initialise( context, attrs, 0 );
    }

  public AAREImageContainerFrame( Context context, AttributeSet attrs, int defStyleAttr )
    {
    super( context, attrs, defStyleAttr );

    initialise( context, attrs, defStyleAttr );
    }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AAREImageContainerFrame( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
    {
    super( context, attrs, defStyleAttr, defStyleRes );

    initialise( context, attrs, defStyleAttr );
    }


  ////////// View Method(s) //////////

  /*****************************************************
   *
   * Called to measure the view.
   *
   *****************************************************/
  @Override
  protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
    {
    mAspectRatioEnforcer.onMeasure( this, widthMeasureSpec, heightMeasureSpec );

    super.onMeasure( mAspectRatioEnforcer.getWidthMeasureSpec(), mAspectRatioEnforcer.getHeightMeasureSpec() );
    }


  /*****************************************************
   *
   * Called with the image size.
   *
   *****************************************************/
  @Override
  public void onSizeChanged( int width, int height, int previousWidth, int previousHeight )
    {
    super.onSizeChanged( width, height, previousWidth, previousHeight );

    mWidth  = width;
    mHeight = height;

    checkRequestImage();
    }


  ////////// IImageConsumer Method(s) //////////

  /*****************************************************
   *
   * Called when the image is downloading.
   *
   *****************************************************/
  @Override
  public void onImageDownloading( Object key )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "onImageDownloading( key = " + key + " )" );

    if ( mShowProgressSpinnerOnDownload ) showProgressSpinner();
    }


  /*****************************************************
   *
   * Called when the image is available.
   *
   *****************************************************/
  @Override
  public void onImageAvailable( Object key, Bitmap bitmap )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "onImageAvailable( key = " + key + ", bitmap = " + bitmap + " )" );

    if ( keyIsOK( key ) )
      {
      // Make sure we don't do anything if we get the bitmap delivered
      // more than once.
      mExpectedKey = null;

      setImageBitmap( bitmap );

      fadeImageIn();
      }
    }


  /*****************************************************
   *
   * Called when an image could not be loaded.
   *
   *****************************************************/
  @Override
  public void onImageUnavailable( Object key, Exception exception )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "onImageUnavailable( key = " + key + ", exception = " + exception + " )" );

    // TODO
    }


  ////////// Animation.AnimationListener Method(s) //////////

  /*****************************************************
   *
   * Called when an animation starts.
   *
   *****************************************************/
  @Override
  public void onAnimationStart( Animation animation )
    {
    // Ignore
    }


  /*****************************************************
   *
   * Called when an animation completes.
   *
   *****************************************************/
  @Override
  public void onAnimationEnd( Animation animation )
    {
    // Clear the animation

    if ( animation == mFadeInAnimation )
      {
      mFadeInAnimation = null;

      mImageView.setAnimation( null );
      }
    }


  /*****************************************************
   *
   * Called when an animation repeats.
   *
   *****************************************************/
  @Override
  public void onAnimationRepeat( Animation animation )
    {
    // Ignore
    }


  ////////// Method(s) //////////

  /*****************************************************
   *
   * Initialises this widget.
   *
   *****************************************************/
  protected void initialise( Context context, AttributeSet attributeSet, int defaultStyle )
    {
    // Get the view

    View view = onCreateView( context, attributeSet, defaultStyle );

    mImageView       = (ImageView)view.findViewById( R.id.image_view );
    mProgressSpinner = (ProgressBar)view.findViewById( R.id.progress_spinner );


    mAspectRatioEnforcer = new AspectRatioEnforcer( context, attributeSet, defaultStyle );


    // Check for specific XML attributes

    if ( attributeSet != null )
      {
      TypedArray typedArray = context.obtainStyledAttributes( attributeSet, R.styleable.ImageContainerFrame, defaultStyle, defaultStyle );

      setShowProgressSpinnerOnDownload( typedArray.getBoolean( R.styleable.ImageContainerFrame_showProgressSpinnerOnDownload, mShowProgressSpinnerOnDownload ) );

      typedArray.recycle();
      }

    }


  /*****************************************************
   *
   * Returns the view for this frame. The view should be
   * attached to this frame when this method returns.
   *
   *****************************************************/
  abstract protected View onCreateView( Context context, AttributeSet attributeSet, int defaultStyle );


  /*****************************************************
   *
   * Sets the aspect ratio for images.
   *
   *****************************************************/
  public void setImageAspectRatio( float aspectRatio )
    {
    mAspectRatioEnforcer.setAspectRatio( aspectRatio );
    }


  /*****************************************************
   *
   * Sets a frame border around the image as a proportion
   * of the image size (which we won't know until we are
   * measured), by setting the padding.
   *
   *****************************************************/
  public void setPaddingProportions( float leftProportion, float topProportion, float rightProportion, float bottomProportion )
    {
    mAspectRatioEnforcer.setPaddingProportions( leftProportion, topProportion, rightProportion, bottomProportion );
    }


  /*****************************************************
   *
   * Sets the image bitmap.
   *
   *****************************************************/
  public void setImageBitmap( Bitmap bitmap )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "setImageBitmap( bitmap = " + bitmap + " )" );

    mImageView.setVisibility( View.VISIBLE );
    mImageView.setImageBitmap( bitmap );

    // Automatically clear any progress spinner, even if the bitmap is null.
    hideProgressSpinner();
    }


  /*****************************************************
   *
   * Shows any progress spinner.
   *
   *****************************************************/
  public void showProgressSpinner()
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "showProgressSpinner()" );

    if ( mProgressSpinner != null )
      {
      mProgressSpinner.setVisibility( View.VISIBLE );
      }
    else
      {
      if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "  mProgressSpinner = null" );
      }
    }


  /*****************************************************
   *
   * Hides any progress spinner.
   *
   *****************************************************/
  public void hideProgressSpinner()
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "hideProgressSpinner()" );

    if ( mProgressSpinner != null ) mProgressSpinner.setVisibility( View.INVISIBLE );
    }


  /*****************************************************
   *
   * Sets whether the progress spinner is shown when an
   * image is being downloaded.
   *
   *****************************************************/
  public void setShowProgressSpinnerOnDownload( boolean showProgressSpinnerOnDownload )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "setShowProgressSpinnerOnDownload( showProgressSpinnerOnDownload = " + showProgressSpinnerOnDownload + " )" );

    mShowProgressSpinnerOnDownload = showProgressSpinnerOnDownload;
    }


  /*****************************************************
   *
   * Sets the source of the image to be scaled to the
   * correct size.
   *
   *****************************************************/
  public void requestScaledImageOnceSized( String imageClass, Object imageSource )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "requestScaledImageOnceSized( imageClass = " + imageClass + ", imageSource = " + imageSource + " )" );

    // Don't do anything if we're already waiting for the image
    if ( imageClass.equals( mRequestImageClass ) && imageSource.equals( mRequestImageSource ) ) return;


    clear();

    mRequestImageClass  = imageClass;
    mRequestImageSource = imageSource;

    checkRequestImage();
    }


  /*****************************************************
   *
   * Sets the source of the image as an asset to be scaled
   * to the correct size.
   *
   *****************************************************/
  public void requestScaledImageOnceSized( Asset asset )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "requestScaledImageOnceSized( asset = " + asset + " )" );

    requestScaledImageOnceSized( AssetHelper.IMAGE_CLASS_STRING_ASSET, asset );
    }


  /*****************************************************
   *
   * Checks if we have everything we need to request
   * the image.
   *
   *****************************************************/
  private void checkRequestImage()
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "checkRequestImage()" );

    if ( mWidth > 0 && mHeight > 0 && mRequestImageSource != null )
      {
      if ( mRequestImageSource instanceof Asset )
        {
        setExpectedKey( mRequestImageSource );

        AssetHelper.requestImage( getContext(), (Asset)mRequestImageSource, null, mWidth, this );
        }
      else if ( mRequestImageSource instanceof URL )
        {
        setExpectedKey( mRequestImageSource );

        ImageAgent.getInstance( getContext() ).requestImage( mRequestImageClass, mRequestImageSource, (URL)mRequestImageSource, null, mWidth, this );
        }
      }
    }


  /*****************************************************
   *
   * Clears the image and sets the key for the next
   * expected image.
   *
   *****************************************************/
  public void clearForNewImage( Object expectedKey )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "clearForNewImage( expectedKey = " + expectedKey + " )" );

    setExpectedKey( expectedKey );

    setImageBitmap( null );
    }


  /*****************************************************
   *
   * Clears the image and sets a wildcard key for the next
   * expected image.
   *
   *****************************************************/
  public void clearForAnyImage()
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "clearForAnyImage()" );

    setExpectedKey( ANY_KEY );

    mImageView.setImageBitmap( null );
    }


  /*****************************************************
   *
   * Clears the image and key.
   *
   *****************************************************/
  public void clear()
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "clear()" );

    clearForNewImage( null );
    }


  /*****************************************************
   *
   * Sets the key to expect.
   *
   *****************************************************/
  public void setExpectedKey( Object key )
    {
    if ( DEBUGGING_IS_ENABLED ) Log.d( LOG_TAG, "setExpectedKey( key = " + key + " )" );

    mExpectedKey = key;
    }


  /*****************************************************
   *
   * Returns true if the supplied key is OK.
   *
   *****************************************************/
  protected boolean keyIsOK( Object key )
    {
    return ( mExpectedKey == ANY_KEY || key.equals( mExpectedKey ) );
    }


  /*****************************************************
   *
   * Starts a fade-in animation on the image.
   *
   *****************************************************/
  private void fadeImageIn()
    {
    mFadeInAnimation = new AlphaAnimation( ALPHA_TRANSPARENT, ALPHA_OPAQUE );
    mFadeInAnimation.setDuration( FADE_IN_ANIMATION_DURATION_MILLIS );
    mFadeInAnimation.setFillAfter( true );
    mFadeInAnimation.setAnimationListener( this );

    mImageView.startAnimation( mFadeInAnimation );
    }


  ////////// Inner Class(es) //////////

  /*****************************************************
   *
   * ...
   *
   *****************************************************/

  }
