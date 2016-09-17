# Kronhjort Inc.
## First-year Project: Visualization, Navigation, Searching, and Route Planning
...or BFST for short, is a course offered to BSc Software Development students at the IT-University of Copenhagen. The goal of the project is to design and develop an efficient map of Denmark based on data from OpenStreetMap(.org), with integrated navigation. The project was done the first medium sized program we wrote in Java, and was developed in groups of 5. The project counts as 15 ECTs. [Full course description.](https://mit.itu.dk/ucs/cb_www/course.sml?course_id=1793092&mode=search&semester_id=1784115&lang=da&print_friendly_p=t&goto=1474139326.000)

![Final solution](http://linkitto.me/wp-content/uploads/2016/06/mapAfter.png)

### Requirements for the final product
The following requirements are taken from the examination project description and describes the minimum functionality expected of the final system. Extensions were encouraged. The system must:
1. allow the user to specify a data file to be used for the application. As a minimum, the system
should support loading a zipped .OSM file.
2. allow the user to save and load the current model to and from a binary format which must be
documented in the report.
3. allow the user to add something to the map, e.g., points of interest.
4. include a default binary file embedded in the jar file, to be loaded if no .OSM file is given at
start-up.
5. draw all roads in the loaded map dataset, using different colors to indicate types of road.
6. where appropriate, draw additional cartographic elements.
7. allow the user to focus on a specific area on the map, either by drawing a rectangle and
zooming in it, or by zooming in the center of the map and pan the map afterwards.
8. adjust the layout when the size of the window changes.
1
9. unobtrusively show (either continuously or on hover) the name of the road closest to the mouse
pointer, e.g., in a status bar.
10. allow the user to search for addresses typed as a single string, with the results being shown
on the map. Ambiguous user input must be handled appropriatedly, e.g., by displaying a list
of possible matches.
11. be able to compute a shortest path on the map between two points somehow specified by the
user (e.g. by typing addresses or clicking on the map).
12. allow the user to choose between routes for biking/walking and for cars. Car routes should
take into account speed limits/expected average speeds. A route for biking/walking should be
the shortest and cannot use highways.
13. feature a coherent user interface for for your map and accompanying functionality. Specifically,
you must decide how the mouse and keyboard is used to interact in a user-friendly and
consistent manner with the user interface.
14. output a textual description of the route found in Item 1 giving appropriate turn instructions.
E.g., “Follow Rued Langgaardsvej for 50m, then turn right onto Røde Mellemvej.”
15. indicate the current zoom level, e.g., in the form of a graphical scalebar.
16. allow the user to change the visual apperance of the map, e.g. , toggle color blind mode.
17. be fast enough that it is convenient to use, even when working with a complete data set for
all of Denmark. The load time for .OSM can be hard to improve, but after the start-up the
user interface should respond reasonably quickly.
Any code that has been written by a group member in the first hand-ins can be reused in the
final product, though it might be better to redesign the system from scratch. Project diaries by the
group can also form the basis for the final report.


### Final Solution
The code for the full solution can be found in this repository. You might also be interested in the [final report](http://kronhjort.duxcloud.com/report.pdf) (DK) or the [full documentation](http://kronhjort.duxcloud.com/). Below is a selection of the implemented features, for the full overview please check the report.
##### Visual Customization
As an feature our group decided to allow users to customize their view of the map. The user is able to choose from three pre-defined themes and create their own themes within the program.
![Visual Customization](http://linkitto.me/wp-content/uploads/2016/06/mapTheme.png)
##### User added Point of Interest
The final program loads most of the common points of interest from the .OSM format. However, users are free to add and save their own POI to the map. Points can later be moved and altered by the user. The panel in the right side allows for quick-navigation to saved POIs.
![Point of Interest](http://linkitto.me/wp-content/uploads/2016/06/mapPOI.png)

##### Navigation, Drag n' Drop and Print Route
Besides the regular navigation system (required), we also implemented a drag n' drop system that allows the user to drag the start and end point to any place on the map. In the right-hand side, the panel shows a list of navigation instructions. These instructions could at any time be saved as an .PDF file. [An example os such a file can be found here.](http://kronhjort.duxcloud.com/preview.pdf)
![Navigation](http://linkitto.me/wp-content/uploads/2016/06/mapNavigation.png)
###### Contributers by alphabetic order; Anders Valsted, Christian Janos Lebeda, Jannik Halberg Homann, Jens Schmidt Hansen and Martin Edvardsen.
