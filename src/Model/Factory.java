package Model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The factory holds a bunch of lists, which can be converted to DataTree objects.
 */
public class Factory {
    private List<PolygonApprox> buildings, cycleways, parkingArea, farmland, forest, plain, grass, industrial,
            sand, railways, residential, navigationRoads, roads_lvl1, roads_lvl2, roads_lvl3, roads_lvl4, roads_lvl5, walkways,
            water, waterway;
    private List<POI> cities;
    private List<Address> address;

    /**
     * Instantiates an empty factory.
     */
    public Factory() {
        buildings = new ArrayList<>();
        farmland = new ArrayList<>();
        walkways = new ArrayList<>();
        cycleways = new ArrayList<>();
        sand = new ArrayList<>();
        navigationRoads = new ArrayList<>();
        roads_lvl1 = new ArrayList<>();
        roads_lvl2 = new ArrayList<>();
        roads_lvl3 = new ArrayList<>();
        roads_lvl4 = new ArrayList<>();
        roads_lvl5 = new ArrayList<>();
        railways = new ArrayList<>();
        forest = new ArrayList<>();
        plain = new ArrayList<>();
        grass = new ArrayList<>();
        industrial = new ArrayList<>();
        water = new ArrayList<>();
        waterway = new ArrayList<>();
        parkingArea = new ArrayList<>();
        residential = new ArrayList<>();
        cities = new ArrayList();
        address = new ArrayList();
    }

    /**
     * Add a PolygonApprox to the list of roads for navigation.
     * @param way The PolygonApprox to be added.
     */
    public void navigationRoad(PolygonApprox way){navigationRoads.add(way);}

    /**
     * Add a PolygonApprox to the list of farmland.
     * @param way The PolygonApprox to be added.
     */
    public void farmland(PolygonApprox way){farmland.add(way);}

    /**
     * Add a PolygonApprox to the list of walkways.
     * @param way The PolygonApprox to be added.
     */
    public void walkway(PolygonApprox way) {walkways.add(way);}

    /**
     * Add a PolygonApprox to the list of cycleways.
     * @param way The PolygonApprox to be added.
     */
    public void cycleway(PolygonApprox way) {cycleways.add(way);}

    /**
     * Add a PolygonApprox to the list of road_lvl1 (The biggest roads).
     * @param way The PolygonApprox to be added.
     */
    public void road_lvl1(PolygonApprox way) {roads_lvl1.add(way);}

    /**
     * Add a PolygonApprox to the list of road_lvl2 (The next to biggest roads).
     * @param way The PolygonApprox to be added.
     */
    public void road_lvl2(PolygonApprox way) {roads_lvl2.add(way);}

    /**
     * Add a PolygonApprox to the list of road_lvl3 (Medium-sized roads).
     * @param way The PolygonApprox to be added.
     */
    public void road_lvl3(PolygonApprox way) {roads_lvl3.add(way);}

    /**
     * Add a PolygonApprox to the list of road_lvl4 (Smaller roads).
     * @param way The PolygonApprox to be added.
     */
    public void road_lvl4(PolygonApprox way) {roads_lvl4.add(way);}

    /**
     * Add a PolygonApprox to the list of road_lvl5 (The smallest roads).
     * @param way The PolygonApprox to be added.
     */
    public void road_lvl5(PolygonApprox way) {roads_lvl5.add(way);}

    /**
     * Add a PolygonApprox to the list of railways.
     * @param way The PolygonApprox to be added.
     */
    public void railway(PolygonApprox way){ railways.add(way);}

    /**
     * Add a PolygonApprox to the list of forest.
     * @param way The PolygonApprox to be added.
     */
    public void forest(PolygonApprox way) {forest.add(way);}

    /**
     * Add a PolygonApprox to the list of plain.
     * @param way The PolygonApprox to be added.
     */
    public void plain(PolygonApprox way) {plain.add(way);}

    /**
     * Add a PolygonApprox to the list of grass.
     * @param way The PolygonApprox to be added.
     */
    public void grass(PolygonApprox way) {grass.add(way);}

    /**
     * Add a PolygonApprox to the list of sand.
     * @param way The PolygonApprox to be added.
     */
    public void sand(PolygonApprox way) {sand.add(way);}

    /**
     * Add a PolygonApprox to the list of water.
     * @param way The PolygonApprox to be added.
     */
    public void water(PolygonApprox way) {water.add(way);}

    /**
     * Add a PolygonApprox to the list of industrial areas.
     * @param way The PolygonApprox to be added.
     */
    public void industrial(PolygonApprox way) {industrial.add(way);}

    /**
     * Add a PolygonApprox to the list of waterways.
     * @param way The PolygonApprox to be added.
     */
    public void waterway(PolygonApprox way) {waterway.add(way);}

    /**
     * Add a PolygonApprox to the list of parking areas.
     * @param way The PolygonApprox to be added.
     */
    public void parking(PolygonApprox way) {parkingArea.add(way);}

    /**
     * Add a PolygonApprox to the list of residential areas.
     * @param way The PolygonApprox to be added.
     */
    public void residential(PolygonApprox way) {residential.add(way);}

    /**
     * Add a PolygonApprox to the list of buildings.
     * @param way The PolygonApprox to be added.
     */
    public void building(PolygonApprox way){ buildings.add(way);}

    /**
     * Add an Address to the list of addresses.
     * @param point The Point2D to be added.
     */
    public void address(Address point) {address.add(point);}

    /**
     * Add a POI to the list of addresses.
     * @param point The Point2D to be added.
     */
    public void cities(POI point){ cities.add(point);}


    /**
     * Converts the roads for navigation to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree navigationRoad(){return new DataTree(navigationRoads, 64, false);}

    /**
     * Converts the walkways to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree walkway() {return new DataTree(walkways, 4, true);}

    /**
     * Converts the cycleways to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree cycleway() {return new DataTree(cycleways, 4, true);}

    /**
     * Converts the roads_lvl1 to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree roads_lvl1() {return new DataTree(roads_lvl1, 16, true);}

    /**
     * Converts the road_lvl2 to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree roads_lvl2() {return new DataTree(roads_lvl2, 16, true);}

    /**
     * Converts the road_lvl3 to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree roads_lvl3() {return new DataTree(roads_lvl3, 16, true);}

    /**
     * Converts the road_lvl4 to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree roads_lvl4() {return new DataTree(roads_lvl4, 16, true);}

    /**
     * Converts the road_lvl5 to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree roads_lvl5() {return new DataTree(roads_lvl5, 16, true);}

    /**
     * Converts the railways to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree railway() {return new DataTree(railways, 8, true);}

    /**
     * Converts the forests to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree forest() {return new DataTree(forest, false);}

    /**
     * Converts the plains to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree plain() {return new DataTree(plain, 8, true);}

    /**
     * Converts the grass to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree grass() {return new DataTree(grass, 16, true);}

    /**
     * Converts the industrial areas to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree industrial() {return new DataTree(industrial, 4, true);}

    /**
     * Converts the residential areas to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree residential() {return new DataTree(residential, 4, true);}

    /**
     * Converts the water areas to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree water() {return new DataTree(water, false);}

    /**
     * Converts the waterways to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree waterway() {return new DataTree(waterway, 8, true);}

    /**
     * Converts the parking areas to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree parking() {return new DataTree(parkingArea, 4, true);}

    /**
     * Converts the buildings to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree buildings() {return new DataTree(buildings, 32, true);}

    /**
     * Converts the farmland to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree farmland(){return new DataTree(farmland, 16, true);}

    /**
     * Converts the sand areas to a DataTree.
     * @return The DataTree created from the list.
     */
    public DataTree sand(){return new DataTree(sand, 16, true);}


    /**
     * Converts the farmland to a DataTree.
     * @return The DataTree created from the list.
     */
    public Address[] address(){
        Collections.sort(address);
        return address.toArray(new Address[address.size()]);
    }

    /**
     * Converts the sand areas to a DataTree.
     * @return The DataTree created from the list.
     */
    public POI[] cities(){
        Collections.sort(cities);
        return cities.toArray(new POI[cities.size()]);
    }
}
