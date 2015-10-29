package ca.ualberta.ssrg.movies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import ca.ualberta.ssrg.androidelasticsearch.R;

public class MainActivity extends Activity{

	private ListView movieList;
	private Movies movies;
	private ArrayAdapter<Movie> moviesViewAdapter;
	private ESMovieManager movieManager;
	private MoviesController moviesController;
	private EditText stringSearch;

	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		movieList = (ListView) findViewById(R.id.movieList);
	}

	@Override
	protected void onStart() {
		super.onStart();

		movies = new Movies();
		moviesViewAdapter = new ArrayAdapter<Movie>(this, R.layout.list_item,movies);
		movieList.setAdapter(moviesViewAdapter);
		movieManager = new ESMovieManager("");

		// Show details when click on a movie
		movieList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,	long id) {
				int movieId = movies.get(pos).getId();
				startDetailsActivity(movieId);
			}

		});

		// Delete movie on long click
		movieList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Movie movie = movies.get(position);
				Toast.makeText(mContext, "Deleting " + movie.getTitle(), Toast.LENGTH_LONG).show();

				Thread thread = new DeleteThread(movie.getId());
				thread.start();

				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		/**
		stringSearch = (EditText) findViewById(R.id.editText1);
		String stringQuery  = stringSearch.getText().toString();

		// you cannot access the network from the gui thread
		// so let's create another thread to do that wrork
		// if we try to use the gui thread -- gui will stop and wait.
thread("*"+ text)
		SearchThread thread = new SearchThread("" +
				"curl -XPOST \"http://cmput301.softwareprocess.es:8080/testing/movie/_search\" " +
				"-d '" +
				"\t\n" +
				"{\n" +
				"  \"query\":{\n" +
				"  \"query_string\":{\n" +
				"   \"" +
				stringQuery +
				",\n" +
				"  \"fields\": [\"title\"]\n" +
				"  }\n" +
				"}\n" +
				"}'\n");

		thread.start();
**/
		/**
		SearchThread thread = new SearchThread("*");
		thrad.start();
		 **/	
	}
	
	/** 
	 * Called when the model changes
	 */
	public void notifyUpdated() {
		// Thread to update adapter after an operation
		Runnable doUpdateGUIList = new Runnable() {
			public void run() {
				moviesViewAdapter.notifyDataSetChanged();
			}
		};
		
		runOnUiThread(doUpdateGUIList);
	}

	/** 
	 * Search for movies with a given word(s) in the text view
	 * @param view
	 */
	public void search(View view) {
		movies.clear();
		stringSearch = (EditText) findViewById(R.id.editText1);
		String queryString =stringSearch.getText().toString();

		SearchThread thread = new SearchThread(queryString);
		thread.start();
		// TODO: Extract search query from text view

		// TODO: Run the search thread
		
	}
	
	/**
	 * Starts activity with details for a movie
	 * @param movieId Movie id
	 */
	public void startDetailsActivity(int movieId) {
		Intent intent = new Intent(mContext, DetailsActivity.class);
		intent.putExtra(DetailsActivity.MOVIE_ID, movieId);

		startActivity(intent);
	}
	
	/**
	 * Starts activity to add a new movie
	 * @param view
	 */
	public void add(View view) {
		Intent intent = new Intent(mContext, AddActivity.class);
		startActivity(intent);
	}



	class SearchThread extends Thread {
		private String search;

		public SearchThread(String search) {
			this.search = search;
		}

		@Override
		public void run() {
			movies.clear();
			movies.addAll(movieManager.searchMovies(search, null));
			notifyUpdated();
		}
		
	}

	
	class DeleteThread extends Thread {
		private int movieId;

		public DeleteThread(int movieId) {
			this.movieId = movieId;
		}

		@Override
		public void run() {
			moviesController.deleteMovie(movieId);

			// Remove movie from local list
			for (int i = 0; i < movies.size(); i++) {
				Movie m = movies.get(i);

				if (m.getId() == movieId) {
					movies.remove(m);
					break;
				}
			}
		}
	}
}