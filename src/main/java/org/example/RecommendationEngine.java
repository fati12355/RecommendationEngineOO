// Project CSI2120/CSI2520
// Winter 2025
// Robert Laganiere, uottawa.ca

package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// this is the class that will generate the recommendations for a user
public class RecommendationEngine {

	public ArrayList<Movie> moviesDatabase; //Create a database with all the movies
	// Create a HashMap to store users by their ID
	Map<Integer, User> usersDatabase = new HashMap<>();
	public void readMovies(String csvFile) throws IOException,
			NumberFormatException {

		String line;
		String delimiter = ","; // Assuming values are separated by commas

		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		// Read each line from the CSV file
		line = br.readLine();

		while ((line = br.readLine()) != null && line.length() > 0) {
			// Split the line into parts using the delimiter
			String[] parts = line.split(delimiter);
			String title;

			// parse the ID
			int movieID= Integer.parseInt(parts[0]);

			if (parts.length < 3)
				throw new NumberFormatException("Error: Invalid line structure: " + line);

			// we assume that the first part is the ID
			// and the last one are genres, the rest is the title
			title= parts[1];
			if (parts.length > 3) {

				for (int i=2; i<parts.length-1; i++)
					title+= parts[i];
			}

			moviesDatabase.add(new Movie(movieID,title));

		}

	}
	public void readRatings(String csvFile) throws IOException,
			NumberFormatException {

		String line;
		String delimiter = ","; // Assuming values are separated by commas

		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		// Read each line from the CSV file
		line = br.readLine();

		while ((line = br.readLine()) != null && line.length() > 0) {
			// Split the line into parts using the delimiter
			String[] parts = line.split(delimiter);

			if (parts.length < 4)
				throw new NumberFormatException("Error: Invalid line structure: " + line);// Each line
			// should at least have the user and movie IDs, the rating and a timestamp

			int currentUserID = Integer.parseInt(parts[0]); //ID of the user doing the rating
			int movieID = Integer.parseInt(parts[1]); // ID of the movie being rated
			Double rating = Double.parseDouble(parts[3]); // the rating score
			ArrayList<Integer> likedMovies = new ArrayList<>(); // a list of all liked movies
			ArrayList<Integer> unlikedMovies = new ArrayList<>(); // a list of all unliked movies
			Double R = 3.5;
			if (rating >= R) { // A rating should be higher than 3.5 to be considered a like
				likedMovies.add(movieID);
			}else{
				unlikedMovies.add(movieID);
			}
			User currentUser = new User(currentUserID,likedMovies,unlikedMovies);
			usersDatabase.put(currentUserID,currentUser); // Add the user in the database

			//Add logic to populate the ratings for all movies
			Movie movieToSetRatingFor; // Temporary container
			HashMap<Integer, Double> ratingAndViewer = new HashMap<>();
			ratingAndViewer.put(currentUserID, rating);
			for ( int m =0; m < moviesDatabase.size(); m++){
				movieToSetRatingFor = moviesDatabase.get(m);
				if (movieToSetRatingFor.getMovieID() == movieID){
					moviesDatabase.get(m).getViewersAndRatings().add(ratingAndViewer);
					break;
				}
			}
		}
	}

	//comment this part before test
	public void findRecommendations(int targetUserID){ // the algorithm to find movies to recommend
		//Get the list of seen movies for this user (liked and unliked movies)
		ArrayList <Movie> seenMovies = usersDatabase.get(targetUserID).getLikedMovies();
		ArrayList <Movie> unLikedMoviesToAdd = usersDatabase.get(targetUserID).getUnlikedMovies();
		Movie dBsMovie; // temporary variable
		int K = 10; // A movie should have been viewed by at least K users to be used for recommendation

		for (int i=0; i< unLikedMoviesToAdd.size() ; i++ ){ // Combine the unliked movies to the liked one
			seenMovies.add(unLikedMoviesToAdd.get(i));
		}
		for (int j = 0; j < moviesDatabase.size(); j++ ){ //for movies in the dataset
			dBsMovie = moviesDatabase.get(j);
			for ( int k = 0; k < seenMovies.size(); k++){
				Movie currentMovie = seenMovies.get(k); // read the movies seen by the user one by one
				if (currentMovie.getMovieID() == dBsMovie.getMovieID()){ //the user already saw the movie
					break;
				}else { // the user has not seen the movie yet. It may be recommended.
					//check if the movie has been viewed by at least K users

				}

				}
			}

	}
	//end of part to comment



	public static void main(String[] args) {

		//Need to call the functions to read the movies and rating files here
		
		//to modify
		try {
			//How to run the code: java RecommendationEngine 44 movies.csv ratings.csv
			RecommendationEngine rec= new RecommendationEngine();
			rec.readMovies(args[1]);

		    // just printing few movies
			for (int i=0; i<20; i++) {
				
				System.out.println(rec.moviesDatabase.get(i).getTitle().toString());
			}
			
        } catch (Exception e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
	}
}
