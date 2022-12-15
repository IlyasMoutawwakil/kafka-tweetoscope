/*
Copyright 2022 Virginie Galtier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>
 */
package tweetoscope;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * 
 * Displays the most popular hashtags as an histogram.
 * <p>
 * Leader board data is received via Java Flow (publisher =
 *{@link distributed_tweetoscope.HashtagCounter})
 *
 * @author Virginie Galtier
 *
 */
@SuppressWarnings("serial")
public class Visualizor extends JFrame implements Subscriber<Map<String, Integer>> {

	/**
	 * Dataset organized with a single row (key = {@code ROW_KEY}), and one column
	 * per hashtag. The column key is the hashtag text, the value stored at
	 * dataset(row:KEY_ROW, col: hashtag) is the number of occurrences of the
	 * hashtag.
	 */
	protected DefaultCategoryDataset dataset;
	/**
	 * Key of the single row of the {@code dataset} that contains the occurrences of
	 * hashtags
	 */
	protected final static String ROW_KEY = "hashtag";

	/**
	 * Number of hashtags to include on the leader board
	 */
	protected int nbLeaders;

	/**
	 * Sets up the graphical representation of the leader board.
	 * 
	 * @param nbLeaders number of hashtags to include on the leader board
	 */
	public Visualizor(int nbLeaders) {

		this.nbLeaders = nbLeaders;

		dataset = new DefaultCategoryDataset();

		JFreeChart chart = ChartFactory.createBarChart("Most Popular Hashtags", // title
				"", // category axis label
				"number of occurences", // value axis label
				dataset, // category dataset
				PlotOrientation.HORIZONTAL, // orientation
				false, // legend
				true, // tooltips
				false); // urls
		chart.getCategoryPlot().setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		chart.getCategoryPlot().setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		chartPanel.setBackground(Color.white);
		chartPanel.setPreferredSize(new Dimension(500, 300));
		this.add(chartPanel);

		this.pack();
		this.setTitle("Tweetoscope");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	/**
	 * Triggered when the data to be displayed on the leader board changes:
	 * introduction of a new hashtag, or update of the number of occurrences of a
	 * hashtag already on the leader board. The new map is sent by the upstream
	 * component (HashtagCounter) via Java Flow).
	 */
	@Override
	public void onNext(Map<String, Integer> item) {
		// setValue triggers the update of the graph
		// Swing and JFreeChart are not thread-safe, updates to the graph should be done
		// by the Event Dispaching Thread
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// clears the leader board from previous data
				dataset.clear();
				// populates the leader board using data from the received map
				// sorted by reverse number of occurrences
				Stream<Entry<String, Integer>> sortedTopHashtags = item.entrySet().stream()
						.sorted(Collections.reverseOrder(Entry.comparingByValue()));
				sortedTopHashtags.forEach(t -> {
					dataset.setValue(t.getValue(), ROW_KEY, t.getKey().toString());
				});
				// adds padding, if necessary (if we have not yet observed as many hashtags as
				// expected for the leader board
				for (int i = item.entrySet().size(); i < nbLeaders; i++) {
					dataset.setValue(0, ROW_KEY, "");
				}
			}
		});
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onError(Throwable throwable) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onComplete() {
		// TODO Auto-generated method stub
	}
}