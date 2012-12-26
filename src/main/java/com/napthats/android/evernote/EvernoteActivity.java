package com.napthats.android.evernote;

import java.io.File;
import java.util.List;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;
//
import android.util.Log;
//

import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteFilter;


public class EvernoteActivity extends Activity
{
  private String EVERNOTE_HOST = "www.evernote.com";

  private EvernoteSession session = null;


  //tentative
  protected final NoteStoreProxy createNoteStore() {
    checkInitialized();
    return (NoteStoreProxy) Proxy.newProxyInstance(
      NoteStoreProxy.class.getClassLoader(),
      new Class[] {NoteStoreProxy.class},
      new InvocationHandler() {
        private NoteStore.Client note_store = null;
        {
          try { 
            note_store = session.createNoteStore();
          }
          catch (Exception e) {
            //TODO: Deal with exceptions.
          }
        }
        public Object invoke(Object proxy, Method proxy_method, Object[] args)
        throws Throwable {
          //TODO: Deal with exceptions.
          //TODO: Deal with few methods without an auth token.
          try {
            Object[] authkey_added_args = new Object[args.length + 1];
            authkey_added_args[0] = session.getAuthToken();
            System.arraycopy(args, 0, authkey_added_args, 1, args.length);
            Class[] authkey_added_argsclass = new Class[args.length + 1];
            authkey_added_argsclass[0] = String.class;
            System.arraycopy(proxy_method.getParameterTypes(), 0, authkey_added_argsclass, 1, args.length);
            Method method = note_store.getClass().getMethod(proxy_method.getName(), authkey_added_argsclass);
            return method.invoke(note_store, authkey_added_args);
          }
          catch (Throwable e) {
            throw e.getCause();
          }
        }
      }
    );
  }

    
  public interface NoteStoreProxy {
    public List<Tag> listTags();
    public Note updateNote(Note note);
    public Tag createTag(Tag tag);
    public Note getNote(String guid, boolean b1, boolean b2, boolean b3, boolean b4);
    public NoteList findNotes(NoteFilter filter, int i1, int i2);
    public Note createNote(Note note);
  }
  

  /**
   * isLoggedIn.
   */
  protected final boolean isLoggedIn() {
    checkInitialized();
    return session.isLoggedIn();
  }


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
   * Authenticate Evernote and set callback following authentication.
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
        assert succeededCallbackAfterAuthentication != null;
        assert failedCallbackAfterAuthentication != null;
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
