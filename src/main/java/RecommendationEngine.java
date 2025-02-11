// Project CSI2120/CSI2520
// Winter 2025
// Robert Laganiere, uottawa.ca

// Name: Fatimata Abdou Dramane
// Student number: 300277601



import java.io.*;
import java.util.*;

// this is the class that will generate the recommendations for a user
//For this algorithm I am making the assumption that each seen movie has been rated (liked or unliked)

public class RecommendationEngine {



	// hey Gee Gee: Changed moviesDatabase from ArrayList to HashMap
	HashMap<Integer, Movie> moviesDatabase = new HashMap<>(); // Create a database with all the movies
	// Create a HashMap to store users by their ID
	Map<Integer, User> usersDatabase = new HashMap<>();
	HashMap <Movie, Float> MScoresForU = new HashMap<>() ; // store the score each movie get for the user U to later sort and recommend the top 20

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

//		int counter = 0; // printing the data in moviesDatabase to ensure it's populated correctly


//		for (Map.Entry<Integer, Movie> entry : moviesDatabase.entrySet()) { // Print the movies we are recommending
//			System.out.println("ID: " + entry.getKey() + " | Title: " + entry.getValue().getTitle());
//			if (++counter >= 1000) break; // Limit to 1000 movies
//		}
	}

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
			Double rating = Double.parseDouble(parts[2]); // the rating score
			currentMovie = moviesDatabase.get(movieID); //the movie on the current line

			// Add logic to populate the ratings for all movies
			if (currentMovie.getViewersAndRatings() != null){
				currentMovie.getViewersAndRatings().put(currentUserID, rating);//update the movie viewer and rating database if it exist
			}else{
				HashMap <Integer, Double> ViewersAndRatings = new HashMap<>();//create a container to store viewers and ratings for the movie
				ViewersAndRatings.put(currentUserID, rating);
				currentMovie.setViewersAndRatings(ViewersAndRatings);
			}

			if (usersDatabase.containsKey(currentUserID)){//if the user already exist in the database, update his/her informations
				if (rating >=R){//the movie has been liked
					usersDatabase.get(currentUserID).getLikedMovies().put(movieID,moviesDatabase.get(movieID));
					currentMovie.incrementLikes();
				}else {//the movie has been disliked
					usersDatabase.get(currentUserID).getUnlikedMovies().put(movieID,moviesDatabase.get(movieID));
				}
			}else{//create the user in the database if he didn't exist before
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
		HashMap<Integer, Movie> seenMovies = U.getLikedMovies(); // hey Gee Gee: Use HashMap
		HashMap<Integer, Movie> unLikedMoviesToAdd = U.getUnlikedMovies(); // hey Gee Gee: Use HashMap
		Movie dBsMovie;
		int K = 10; // A movie should have been viewed by at least K users to be used for recommendation

		// hey Gee Gee: Add all unliked movies to seenMovies
		for (Map.Entry<Integer, Movie> entry : unLikedMoviesToAdd.entrySet()) {
			seenMovies.put(entry.getKey(), entry.getValue());
		}

		boolean found; // variable to check if a user has seen a specific movie
		float scoreOfUforM =0f ; // "probability" that U will like M
		int LofM = 0; //Number of user that liked the movie and that we used to compute the score
		float sOfUandV; // the score of U and V (the level at which they have similar tastes)
		int commonLikedMovies;
		int commonUnlikedMovies;
		int bothSeenMovies;
		int movie_ID;
		float probability;

		// hey Gee Gee: Iterate over the HashMap values instead of an ArrayList
		for (Movie movie : moviesDatabase.values()) {
			movie_ID = movie.getMovieID();
			found = seenMovies.containsKey(movie_ID); // hey Gee Gee: Check if the movie is in seenMovies
			if (!found) { // the user has not seen the movie yet. It may be recommended.
				// hey Gee Gee: Logic to check if the movie has been liked by at least K users to be added
				if (movie.getNumberOfLikes() > K){
					scoreOfUforM = 0f;
					LofM = 0;
					for (User V: usersDatabase.values()){
						commonLikedMovies = 0;
						commonUnlikedMovies = 0;
						bothSeenMovies = 0;
						if (V.getUserID() != targetUserID && V.getLikedMovies().containsKey(movie_ID)){
							sOfUandV = 0f;// Compute the score of U with V. You might want to use loops.
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
								if (! seenMovies.containsKey(V_slikedMovie.getMovieID())){
									bothSeenMovies++;
								}
							}
							for (Movie V_sUnlikedMovie : V.getUnlikedMovies().values()){//Complete the union with the movies V saw and not U (those V unliked now)
								if (! seenMovies.containsKey(V_sUnlikedMovie.getMovieID())){
									bothSeenMovies++;
								}
							}
//							System.out.print("liked" + commonLikedMovies + "unliked" + commonUnlikedMovies + "bothSeenmovie" + bothSeenMovies);//just testing

							sOfUandV= (commonLikedMovies + commonUnlikedMovies) / (float) bothSeenMovies;
							scoreOfUforM += sOfUandV; //Update the probability for U to like movie M
//							System.out.print("sOfUandV"+sOfUandV);//just testing
//							System.out.print("scoreOfUforM"+scoreOfUforM);//just testing
							LofM++;// Take into account the number of users used for better approximation
//							System.out.print("LofM"+LofM);//just testing
						}
					}
					probability =  (LofM != 0) ? scoreOfUforM / ((float)LofM):0f; //probability for movie to be liked by U (LofM should always be non-zero)
//					//System.out.println("probability"+probability);//just testing

					// store all probabilities with their movie ID and title in a sorted hashmap and maintain the first N
					MScoresForU.put(movie,probability);
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
			String output;
			List<HashMap.Entry<Movie, Float>> recommendableMovies = new ArrayList<>(rec.MScoresForU.entrySet());
			recommendableMovies.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

			PrintWriter writer = new PrintWriter(new FileWriter("src/output/results.txt"));//text file to store the recommended movies
			writer.println("Recommendations for user " + targetUserID + ":");
			writer.println();
			int counter = 0; // printing the data in moviesDatabase to ensure it's populated correctly
			for (Map.Entry<Movie, Float> entry : recommendableMovies) { // Print the movies we are recommending
				output = "Probability: " + entry.getValue() + " | Movie " + (counter + 1) + ": " + entry.getKey().getTitle();
				System.out.println(output);
				writer.println (output);
				if (++counter >= 20) break; // Limit to 20 movies
			}

			writer.close();

//			 hey Gee Gee: Print the first 20 movies with the highest probabilities of being liked by the user




		} catch (Exception e) {
			System.err.println("Error reading the file: " + e.getMessage());
		}
	}
}


