package com.mobeta.android.dslv;

import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.andrewkiluk.machmusicplayer.R;
import com.andrewkiluk.machmusicplayer.models.CurrentData;
import com.andrewkiluk.machmusicplayer.models.PlayerOptions;
import com.andrewkiluk.machmusicplayer.models.Song;


public class DSLVFragment extends ListFragment {

	public ArrayAdapter<Song> adapter;

	public String[] array;
	public ArrayList<String> list;

	public DragSortListView.DropListener onDrop =
			new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				//				Song item = adapter.getItem(from);
				//				adapter.remove(item);
				//				adapter.insert(item, to);

				// This is all KINDS of fucked up. Log the values of from and to and see what's up.

				Log.d("DSLV", "from / to:" + from + " / " + to);
				//				
				if (from < to){
					for (int i = from; i < to; i++){
						Collections.swap(CurrentData.currentPlaylist.songs, i, i+1);
					}
				}
				else{
					for (int i = from; i > to; i--){
						Collections.swap(CurrentData.currentPlaylist.songs, i, i-1);
					}
				}

				adapter.notifyDataSetChanged();

				if (PlayerOptions.isShuffle){
					// The shuffle scheme is interrupted here, so reset the history and queue.
					CurrentData.shuffleReset();
					CurrentData.currentSongIndex = 0;
				}
				else{
					Log.d("DSLV", "Old song index: " + CurrentData.currentSongIndex);
					if(from < CurrentData.currentSongIndex && CurrentData.currentSongIndex < to){
						CurrentData.currentSongIndex--;
						CurrentData.currentPlaylistPosition--; 
					}
					else if(to < CurrentData.currentSongIndex && CurrentData.currentSongIndex < from){
						CurrentData.currentSongIndex++;
						CurrentData.currentPlaylistPosition++; 
					}
					else if(from == CurrentData.currentSongIndex){
						CurrentData.currentSongIndex = to;
						CurrentData.currentPlaylistPosition = to;
					}
					else if(to == CurrentData.currentSongIndex){
						if(from > to){
							CurrentData.currentSongIndex++;
							CurrentData.currentPlaylistPosition++; 
						}
						if (to > from){
							CurrentData.currentSongIndex--;
							CurrentData.currentPlaylistPosition--; 
						}
					}
					
					Log.d("DSLV", "New song index: " + CurrentData.currentSongIndex);
					// HANDLE THE CASES WHEN THE CURRENT POSITION IS EQUAL TO ONE OF FROM OR TO
					
					
					
				}

			}
		}
	};

	private DragSortListView.RemoveListener onRemove = 
			new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			adapter.remove(adapter.getItem(which));
		}
	};

	protected int getLayout() {
		// this DSLV xml declaration does not call for the use
		// of the default DragSortController; therefore,
		// DSLVFragment has a buildController() method.
		return R.layout.fragment_playlist;
	}

	/**
	 * Return list item layout resource passed to the ArrayAdapter.
	 */
	protected int getItemLayout() {
		return R.layout.list_item_handle_left;
	}

	private DragSortListView mDslv;
	private DragSortController mController;

	public int dragStartMode = DragSortController.ON_DOWN;
	public boolean removeEnabled = false;
	public int removeMode = DragSortController.FLING_REMOVE;
	public boolean sortEnabled = true;
	public boolean dragEnabled = true;

	public static DSLVFragment newInstance() {
		DSLVFragment f = new DSLVFragment();
		return f;
	}

	public DragSortController getController() {
		return mController;
	}

	/**
	 * Called from DSLVFragment.onActivityCreated(). Override to
	 * set a different adapter.
	 */
	public void setListAdapter() {

	}

	/**
	 * Called in onCreateView. Override this to provide a custom
	 * DragSortController.
	 */
	public DragSortController buildController(DragSortListView dslv) {
		// defaults are
		//   dragStartMode = onDown
		//   removeMode = flingRight
		DragSortController controller = new DragSortController(dslv);
		//controller.setDragHandleId(R.id.drag_handle);
		controller.setRemoveEnabled(removeEnabled);
		controller.setSortEnabled(sortEnabled);
		controller.setDragInitMode(dragStartMode);
		controller.setRemoveMode(removeMode);
		return controller;
	}





	/** Called when the activity is first created. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mDslv = (DragSortListView) inflater.inflate(getLayout(), container, false);

		mController = buildController(mDslv);
		mDslv.setFloatViewManager(mController);
		mDslv.setOnTouchListener(mController);
		mDslv.setDragEnabled(dragEnabled);

		return mDslv;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDslv = (DragSortListView) getListView(); 

		mDslv.setDropListener(onDrop);
		mDslv.setRemoveListener(onRemove);

		setListAdapter();
	}

}
