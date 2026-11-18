**ANIMO MAP: PROJECT DESCRIPTION AND FEATURES**

**(PROJECT\_DESCRIPTION.md)**

1. **PROJECT OVERVIEW**  
   Animo Map is a proposed Android application for DLSU Manila students. It combines campus navigation, place reviews, student recommendations, and private groups in one app. The first version will be a working multi-user application. Detailed indoor directions will cover a small set of buildings mapped by the project team. Other buildings can still appear on the map as destinations.  
   The application will also provide a community platform where students can share campus-related posts, discover recommended places, and interact with other students through comments and upvotes. To maintain the quality and reliability of publicly displayed content, community posts and proposed map locations will undergo moderator review before publication.  
     
2. **INTENDED USERS**  
* Students: Students with verified DLSU email accounts can use the application's map, search for destinations, get directions, rate and review places, participate in community discussions, submit recommendations, and create or join private groups.  
* Moderators: Designated moderators review community submissions, approve or reject posts, and verify student-recommended locations before they are added to the public map.

3. **MAIN FEATURES**  
   **3.1 Campus map and search**  
* Find buildings, classrooms, restrooms, food places, and areas for studying, waiting, or meeting friends.  
* Search by building name, classroom number, or place name.  
* Display matching search results with relevant building and floor information.  
* Allow users to select a search result and locate it on the map.  
* Filter map markers by location category, including buildings, food places, restrooms, study areas, and waiting or hangout areas.  
* Allow users to clear selected category filters.  
* Include food places on campus and nearby.  
* Display recently added places, consisting of student recommendations that have been approved and verified for public map placement.  
* Allow users to select a recently added place and view its map marker and details.  
  **3.2 Walking directions**  
* Get directions along mapped campus paths and, in selected buildings, through entrances, corridors, stairs, and elevators.  
* Allow users to manually select their starting location or use their current location when location permission is granted.  
* Confirm a starting building and floor before requesting indoor directions.  
* Compare the shortest route with a route that uses fewer stairs when both are available.  
* Allow users to select their preferred route.  
* See each route's distance and estimated walking time.  
* Use Mapbox Directions API for outdoor walking routes only if supported. Indoor and other custom campus routes will use paths mapped by the project team.  
  **3.3 Classroom finder**  
* Search for a classroom by room number.  
* See its building and floor.  
* View the classroom's location on the campus map.  
* Get indoor directions if its building is covered by the pilot map.  
* Classroom availability is not shown.  
  **3.4 Ratings and reviews**  
* Food places: overall experience, rated from 1 (poor) to 5 (excellent).  
* Study, waiting, and hangout areas: comfort, noise level, and temperature.  
* Restrooms: comfort, privacy, and cleanliness.  
* Allow students to submit written reviews for applicable locations.  
* Place pages show written reviews, the number of ratings, and average scores.  
* Allow users to browse reviews by sorting them according to Most Recent, Most Relevant, and All Time.  
* Ratings and reviews are available for existing public map locations and verified, moderator-approved community-recommended locations.

  For noise, 1 means quiet and 5 means loud. For temperature, 1 means cold and 5 means hot. These scores describe the place's conditions.

  **3.5 Student forum and recommended places**

* Students can create community posts under the following categories:  
  * New Place Recommendation  
  * Campus Updates  
  * Campus Tips & Discoveries  
  * General Discussion  
* New Place Recommendations require a place name, selected or pinned location, description, and optional photo.  
* Other students can comment on and upvote approved community posts.  
* Posts can be viewed by recency or popularity.  
* All submitted community posts undergo moderator review before appearing in the public Community Feed.  
* Submitted posts are assigned Pending, Approved, or Rejected statuses.  
* Students can view the approval status of their own submissions.  
* Only approved posts appear in the public Community Feed.  
* A New Place Recommendation that has been approved as a community post must undergo a separate location verification and approval process before it can become a public map point.  
* Once verified and approved for map placement, the recommended location appears as a public map marker and can be viewed and reviewed by other students.  
  **3.6 Popular spots**  
* The app highlights existing locations receiving recent upvotes and reviews.  
* These places are called "hotspots." In this app, the term means popular places, not Wi-Fi areas.  
* A hotspot label is added to an existing place rather than creating a duplicate listing.  
* Popular hotspots are highlighted in a dedicated Popular Hotspots section.  
* Hotspot indicators are based on the popularity of existing public map locations, including verified community-recommended places.  
  **3.7 Private groups and campus presence**  
* Students can create invite-only groups, also referred to as circles.  
* Students can join groups through an invitation method, such as a group or circle code.  
* Members can view their joined groups, group information, and other members' profiles.  
* Members may choose to share which campus building they are in while the app is open.  
* Group members see the building and the time the location was last updated.  
* Sharing can be turned off at any time and stops when the app closes.  
* Joining a group does not automatically turn on location sharing.  
* The app does not show an exact room, floor, route, or location history.

	**3.8 User Accounts and Profiles**

* Students must register and log in using a verified DLSU email account to access the application's social features.  
* Users can create and edit their profiles.  
* Each user can create a unique username that serves as their public identity within the application.  
* Users can add or change their profile picture and user description.  
* Users may provide additional profile information, such as their college and academic program.  
* Users can view their own community posts and submit reviews through their profile.  
* Users can view other students' public profiles by selecting their usernames or profile pictures from community posts, comments, and group member lists.  
* Other users' profiles display their public information and community contributions without allowing profile modification.  
* Email addresses and other private account information are not displayed as public profile information.

4. **DATA AND PERMISSIONS**  
* Social features require a verified DLSU email account.  
* Posts, comments, ratings, groups, user profiles, and approved map points are shared across devices.  
* Community posts must be approved by moderators before becoming visible in the public Community Feed.  
* New Place Recommendations require separate location verification and approval before becoming public map points.  
* Location permission is optional. Students can select a starting point manually.  
* Joining a group does not automatically turn on location sharing.  
* Campus presence sharing only displays the user's selected or detected campus building and the time it was last updated.  
* Indoor directions use only routes that the project team has mapped.  
* Public profiles display usernames and selected profile information rather than private account details.

5. **OUT OF SCOPE FOR THE FIRST VERSION**  
* Live classroom availability.  
* Indoor directions for every DLSU Manila building.  
* Automatic detection of a student's exact indoor location.  
* Background or always-on location tracking.  
* Fitness activity recording and route history.  
* Wi-Fi coverage ratings.  
* Automatic addition of popular posts to the map without moderator verification.  
* Walking times based on live crowd levels.

6. **SUCCESS CRITERIA**  
* A student can find a classroom in a pilot building and compare available routes and walking times.  
* A student can find and review a food place, restroom, or waiting area.  
* A student can search for locations and browse map markers using category filters.  
* A student can submit a community post and view its moderation status.  
* Only approved community posts appear in the public Community Feed.  
* A student recommendation becomes a public map point only after moderator approval and location verification.  
* Students can discover recently added places and popular hotspots through the map.  
* Students can interact with approved community posts through comments and upvotes.  
* Students can create and edit their profiles, use unique usernames, and view other students' public profiles.  
* Students can create or join private groups and view their group members.  
* Students on different devices can join a private group and see a member's building when that member chooses to share it.  
* Turning sharing off or closing the app stops further location updates.

7. **PROJECT ASSUMPTIONS**  
* The team will choose pilot buildings it can accurately survey and is allowed to map.  
* Nearby food places will be shown as destinations, but directions inside those establishments are not required.  
* The team will decide the exact DLSU email verification method during technical design.  
* The team will determine the criteria for identifying popular hotspots based on recent community engagement.  
* Moderators will review community submissions and verify proposed locations before they are made publicly available on the map.  
* The application will use Mapbox for its interactive map and supported outdoor walking route services. Custom campus location data and indoor navigation paths will be provided by the project team.

