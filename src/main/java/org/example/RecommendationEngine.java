// Project CSI2120/CSI2520
// Winter 2025
// Robert Laganiere, uottawa.ca

package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// this is the class that will generate the recommendations for a user
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RecommendationEngine {

	// hey Gee Gee: Changed moviesDatabase from ArrayList to HashMap
	public HashMap<Integer, Movie> moviesDatabase = new HashMap<>(); // Create a database with all the movies
	// Create a HashMap to store users by their ID
	Map<Integer, User> usersDatabase = new HashMap<>();

	public void readMovies(String csvFile) throws IOException, NumberFormatException {
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
			int movieID = Integer.parseInt(parts[0]);

			if (parts.length < 3)
				throw new NumberFormatException("Error: Invalid line structure: " + line);

			// hey Gee Gee: Handle title extraction
			// we assume that the first part is the ID and the last one are genres, the rest is the title
			title = parts[1];
			if (parts.length > 3) {
				for (int i = 2; i < parts.length - 1; i++)
					title += parts[i];
			}

			// hey Gee Gee: Add the movie to the HashMap
			moviesDatabase.put(movieID, new Movie(movieID, title));
		}
	}

	public void readRatings(String csvFile) throws IOException, NumberFormatException {
		String line;
		String delimiter = ","; // Assuming values are separated by commas

		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		// Read each line from the CSV file
		line = br.readLine();

		while ((line = br.readLine()) != null && line.length() > 0) {
			// Split the line into parts using the delimiter
			String[] parts = line.split(delimiter);

			if (parts.length < 4)
				throw new NumberFormatException("Error: Invalid line structure: " + line);

			int currentUserID = Integer.parseInt(parts[0]); // ID of the user doing the rating
			int movieID = Integer.parseInt(parts[1]); // ID of the movie being rated
			Double rating = Double.parseDouble(parts[3]); // the rating score
			HashMap<Integer, Movie> likedMovies = new HashMap<>(); // hey Gee Gee: Changed to HashMap
			HashMap<Integer, Movie> unlikedMovies = new HashMap<>(); // hey Gee Gee: Changed to HashMap
			Double R = 3.5;

			// hey Gee Gee: Check if the rating qualifies as a like
			if (rating >= R) {
				// hey Gee Gee: Add the movie to likedMovies using HashMap
				if (moviesDatabase.containsKey(movieID)) {
					likedMovies.put(movieID, moviesDatabase.get(movieID));
					moviesDatabase.get(movieID).incrementLikes(); // Increment likes for the movie
				}
			} else {
				// hey Gee Gee: Add the movie to unlikedMovies using HashMap
				if (moviesDatabase.containsKey(movieID)) {
					unlikedMovies.put(movieID, moviesDatabase.get(movieID));
				}
			}

			// hey Gee Gee: Update User class to use HashMap for likedMovies and unlikedMovies
			User currentUser = new User(currentUserID, likedMovies, unlikedMovies);
			usersDatabase.put(currentUserID, currentUser); // Add the user in the database

			// Add logic to populate the ratings for all movies
			HashMap<Integer, Double> ratingAndViewer = new HashMap<>();
			ratingAndViewer.put(currentUserID, rating);

			// hey Gee Gee: Access the movie directly using the HashMap
			if (moviesDatabase.containsKey(movieID)) {
				moviesDatabase.get(movieID).getViewersAndRatings().add(ratingAndViewer);
			}
		}
	}

	// comment this part before test
	public void findRecommendations(int targetUserID) {
		// the algorithm to find movies to recommend

		HashMap<Integer, Movie> seenMovies = usersDatabase.get(targetUserID).getLikedMovies(); // hey Gee Gee: Updated to HashMap
		HashMap<Integer, Movie> unLikedMoviesToAdd = usersDatabase.get(targetUserID).getUnlikedMovies(); // hey Gee Gee: Updated to HashMap
		Movie dBsMovie;
		int K = 10; // A movie should have been viewed by at least K users to be used for recommendation

		// hey Gee Gee: Add all unliked movies to seenMovies
		for (Map.Entry<Integer, Movie> entry : unLikedMoviesToAdd.entrySet()) {
			seenMovies.put(entry.getKey(), entry.getValue());
		}

		boolean found; // variable to check if a user has seen a specific movie

		// hey Gee Gee: Iterate over the HashMap values instead of an ArrayList
		for (Movie movie : moviesDatabase.values()) {
			found = seenMovies.containsKey(movie.getMovieID()); // hey Gee Gee: Check if the movie is in seenMovies
			if (!found) { // the user has not seen the movie yet. It may be recommended.
				// hey Gee Gee: Logic to check if the movie has been liked by at least K users to be added
			}
		}
	}
	// end of part to comment

	public static void main(String[] args) {
		try {
			RecommendationEngine rec = new RecommendationEngine();
			rec.readMovies(args[1]);

			// hey Gee Gee: Access movies from the HashMap for testing
			int counter = 0;
			for (Movie movie : rec.moviesDatabase.values()) {
				System.out.println(movie.getTitle());
				if (++counter >= 20) break; // Limit to 20 movies
			}
		} catch (Exception e) {
			System.err.println("Error reading the file: " + e.getMessage());
		}
	}
}

