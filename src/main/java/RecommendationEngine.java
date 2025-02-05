// Project CSI2120/CSI2520
// Winter 2025
// Robert Laganiere, uottawa.ca

import java.io.*;
import java.util.*;

// this is the class that will generate the recommendations for a user
//For this algorithm I am making the assumption that each seen movie has been rated (liked or unliked)

public class RecommendationEngine {



	// hey Gee Gee: Changed moviesDatabase from ArrayList to HashMap
	HashMap<Integer, Movie> moviesDatabase = new HashMap<>(); // Create a database with all the movies
	// Create a HashMap to store users by their ID
	Map<Integer, User> usersDatabase = new HashMap<>();
	TreeMap <Double, Movie> MScoresForU = new TreeMap<>(Comparator.reverseOrder()) ; // store the score each movie get for the user U to later sort and recommend the top 20

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
		br.close();
	}

	//this function  has to be rewritten. Check if the user exist and add liked or unliked movie
	public void readRatings(String csvFile) throws IOException, NumberFormatException { //his function reads the rating
		//file and populate the viewers and ratings for each movie. It also populate the users Database
		String line;
		String delimiter = ","; // Assuming values are separated by commas

		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		// Read each line from the CSV file
		line = br.readLine();
		Double R = 3.5; // rating should be at least 3.5 on a 5 scale to be considered a like
		Movie currentMovie; //temporary variable to iterate through all the movies

		while ((line = br.readLine()) != null && line.length() > 0) {
			// Split the line into parts using the delimiter
			String[] parts = line.split(delimiter);

			if (parts.length < 4)
				throw new NumberFormatException("Error: Invalid line structure: " + line);

			Integer currentUserID = Integer.parseInt(parts[0]); // ID of the user doing the rating
			Integer movieID = Integer.parseInt(parts[1]); // ID of the movie being rated
			Double rating = Double.parseDouble(parts[3]); // the rating score
			currentMovie = moviesDatabase.get(movieID);

			// Add logic to populate the ratings for all movies
			if (currentMovie.getViewersAndRatings() != null){
				currentMovie.getViewersAndRatings().put(currentUserID, rating);
			}else{
				HashMap <Integer, Double> ViewersAndRatings = new HashMap<>();
				ViewersAndRatings.put(currentUserID, rating);
				currentMovie.setViewersAndRatings(ViewersAndRatings);
			}

			if (usersDatabase.containsKey(currentUserID)){
				if (rating >=R){
					usersDatabase.get(currentUserID).getLikedMovies().put(movieID,moviesDatabase.get(movieID));
					currentMovie.incrementLikes();
				}else{
					usersDatabase.get(currentUserID).getUnlikedMovies().put(movieID,moviesDatabase.get(movieID));
				}
			}else{
				HashMap<Integer, Movie> likedMovies = new HashMap<>(); // hey Gee Gee: Changed to HashMap
				HashMap<Integer, Movie> unlikedMovies = new HashMap<>(); // hey Gee Gee: Changed to HashMap
				if (rating >=R){
					likedMovies.put(movieID,moviesDatabase.get(movieID));
				}else{
					unlikedMovies.put(movieID,moviesDatabase.get(movieID));
				}
				User currentUser = new User(currentUserID, likedMovies, unlikedMovies); //create a new user
				usersDatabase.put(currentUserID, currentUser); // Add the user in the database

			}
		}
		br.close();
	}

	// comment this part before test

	public void findRecommendations(int targetUserID) {
		// the algorithm to find movies to recommend

		User U = usersDatabase.get(targetUserID);
		HashMap<Integer, Movie> seenMovies = U.getLikedMovies(); // hey Gee Gee: Updated to HashMap
		HashMap<Integer, Movie> unLikedMoviesToAdd = U.getUnlikedMovies(); // hey Gee Gee: Updated to HashMap
		Movie dBsMovie;
		int K = 10; // A movie should have been viewed by at least K users to be used for recommendation

		// hey Gee Gee: Add all unliked movies to seenMovies
		for (Map.Entry<Integer, Movie> entry : unLikedMoviesToAdd.entrySet()) {
			seenMovies.put(entry.getKey(), entry.getValue());
		}

		boolean found; // variable to check if a user has seen a specific movie
		float scoreOfUforM =0 ; // "probability" that U will like M
		int LofM = 0; //Number of user that liked the movie and that we used to compute the score
		float sOfUandV = 0; // the score of U and V (the level at which they have similar tastes)
		int commonLikedMovies;
		int commonUnlikedMovies;
		int bothSeenMovies;
		int movie_ID;
		double probability;

		// hey Gee Gee: Iterate over the HashMap values instead of an ArrayList
		for (Movie movie : moviesDatabase.values()) {
			movie_ID = movie.getMovieID();
			found = seenMovies.containsKey(movie_ID); // hey Gee Gee: Check if the movie is in seenMovies
			if (!found) { // the user has not seen the movie yet. It may be recommended.
				// hey Gee Gee: Logic to check if the movie has been liked by at least K users to be added
				if (movie.getNumberOfLikes() > K){
					scoreOfUforM = 0;
					LofM = 0;
					for (User V: usersDatabase.values()){
						commonLikedMovies = 0;
						commonUnlikedMovies = 0;
						bothSeenMovies = 0;
						if (V.getUserID() != targetUserID && V.getLikedMovies().containsKey(movie_ID)){
							sOfUandV = 0;// Compute the score of U with V. You might want to use loops.
							for (Movie U_Liked : U.getLikedMovies().values()){ // Compute the number of movies they both liked
								if (V.getLikedMovies().containsKey(U_Liked.getMovieID())){
									commonLikedMovies++;
								}
							}
							for (Movie U_Unliked : U.getUnlikedMovies().values()){ // Compute the number of movies they both disliked
								if (V.getUnlikedMovies().containsKey(U_Unliked.getMovieID())){
									commonUnlikedMovies++;
								}
							}
							bothSeenMovies = seenMovies.size();
							for (Movie V_slikedMovie : V.getLikedMovies().values()){//Complete the union with the movies V saw and not U (those V liked for now)
								if (! seenMovies.containsKey(V_slikedMovie.getMovieID())){//should not be "!"
									bothSeenMovies++;
								}
							}
							for (Movie V_sUnlikedMovie : V.getUnlikedMovies().values()){//Complete the union with the movies V saw and not U (those V liked for now)
								if (! seenMovies.containsKey(V_sUnlikedMovie.getMovieID())){ //should not be "!"
									bothSeenMovies++;
								}
							}
							sOfUandV= (commonLikedMovies + commonUnlikedMovies) / bothSeenMovies;
							scoreOfUforM += sOfUandV; //Update the probability for U to like movie M
							LofM++;// Take into account the number of users used for better approximation
							System.out.println(LofM);//just testing
						}

						probability = (double) scoreOfUforM / LofM; //probability for movie to be liked by U
						// store all probabilities with their movie ID and title in a sorted hashmap and maintain the first N
						MScoresForU.put(probability, movie);
					}
				}
			}


		}

	}
	// end of part to comment

	public static void main(String[] args) {
		try {
			RecommendationEngine rec = new RecommendationEngine();
			int targetUserID = Integer.parseInt(args[0]);
			rec.readMovies(args[1]);
			rec.readRatings(args[2]);
			rec.findRecommendations(targetUserID);


			// hey Gee Gee: Print the first 20 movies with the highest probabilities of being liked by the user
			int counter = 0;
//			for (Map.Entry<Integer, Movie> entry : rec.moviesDatabase.entrySet()) {
//				System.out.println("movie ID: " + entry.getKey() + " | Movie title: " + entry.getValue().getTitle() +" | likes:" + + entry.getValue().getNumberOfLikes());
//
//				if (++counter >= 20) break; // Limit to 20 movies
//			}

//			for (Movie movie : rec.moviesDatabase.values()) {
//				System.out.println(movie.getTitle() + "|" + movie.getMovieID());
//				if (++counter >= 20) break; // Limit to 20 movies
//			}

//			for (Map.Entry<Integer, User> entry : rec.usersDatabase.entrySet()) {
//				System.out.println("ID: " + entry.getKey());
//				HashMap<Integer,Movie> liked = entry.getValue().getLikedMovies();
//				HashMap<Integer,Movie> unliked = entry.getValue().getLikedMovies();
//				System.out.println("liked movies");
//
//				for (Map.Entry<Integer, Movie> entr : liked.entrySet()) {
//					System.out.println("Score: " + entr.getKey() + " | Movie: " + entr.getValue().getTitle());
//				}
//				System.out.println("unliked movies");
//
//				for (Map.Entry<Integer, Movie> ent : unliked.entrySet()) {
//					System.out.println("Score: " + ent.getKey() + " | Movie: " + ent.getValue().getTitle());
//				}
//					if (++counter >= 5) break; // Limit to 20 movies
//			}

//			for (Map.Entry<Double, Movie> entry : rec.MScoresForU.entrySet()) { // Print the movies we are recommending
//				System.out.println("Score: " + entry.getKey() + " | Movie: " + entry.getValue().getTitle());
//
//				if (++counter >= 20) break; // Limit to 20 movies
//			}



		} catch (Exception e) {
			System.err.println("Error reading the file: " + e.getMessage());
		}
	}
}

