package com.napthats.android.evernote;

import java.io.File;

import android.app.Activity;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;

import com.evernote.client.oauth.android.EvernoteSession;


public class EvernoteActivity extends Activity
{
  private String EVERNOTE_HOST = "www.evernote.com";

  private EvernoteSession session = null;


  /**
   * Initialize settings of Evernote.
   * This method have to be called before all other methods.
   *
   * @param c_key The consumer key.
   * @param c_secret The consumer secret.
   * @param evernote_host (optional) The hostname for the Evernote. "www.evernote.com" is used
   * for default. This cannot be omitted if temp_dir_name is provided.
   * @param temp_dir_name (optional) The data directory to store evernote files. Evernote sdk's
   * default setting is used for default. This cannot be provided if evernote_host is ommited.
   */
  protected final void initEvernote(String c_key, String c_secret, String evernote_host, String temp_dir_name) {
    _initEvernote(c_key, c_secret, evernote_host, temp_dir_name);
  }

  protected final void initEvernote(String c_key, String c_secret, String evernote_host) {
    _initEvernote(c_key, c_secret, evernote_host, null);
  }

  protected final void initEvernote(String c_key, String c_secret) {
    _initEvernote(c_key, c_secret, EVERNOTE_HOST, null);
  }

  private void _initEvernote(String c_key, String c_secret, String evernote_host, String temp_dir_name) {
    File temp_file = null;
    if (temp_dir_name != null) {
      temp_file = new File(Environment.getExternalStorageDirectory(), temp_dir_name);
    }
    session = EvernoteSession.init((android.content.Context)this, c_key, c_secret, evernote_host, temp_file);
  }


  /**
   * Authenticate Evernote and set callback after authentication.
   *
   * @param succeeded Callback after succeeding authentication.
   * @param failed Callback after failing authentication.
   */
  protected final void authenticate(CallBack succeeded, CallBack failed) {
      checkInitialized();
      succeededCallbackAfterAuthentication = succeeded;
      failedCallbackAfterAuthentication = failed;
      session.authenticate(this);
  }
    
  private CallBack succeededCallbackAfterAuthentication = null;
  private CallBack failedCallbackAfterAuthentication = null;

  protected interface CallBack {
    public void call();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (session == null) {return;}
    if (requestCode == EvernoteSession.REQUEST_CODE_OAUTH) {
        if (resultCode == Activity.RESULT_OK) {
          succeededCallbackAfterAuthentication.call();
        }
        else
        {
          failedCallbackAfterAuthentication.call();
        }
    }
  }


  private void checkInitialized() {
    if (session == null) {throw new NotInitializedException();}
  }

  public class NotInitializedException extends RuntimeException {}
}
