package lu.kbra.teinn_esp32_frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import lu.pcy113.pclib.swing.JLineGraph;
import lu.pcy113.pclib.swing.JLineGraph.ChartData;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private MqttListener listener;

	private JLineGraph temperatureGraph;
	private JLineGraph.ChartData temperatureSeries;

	private JLineGraph humidityGraph;
	private JLineGraph.ChartData humiditySeries;

	private JLineGraph lightGraph;
	private JLineGraph.ChartData lightSeries;

	private JLineGraph averageGraph;
	private JLineGraph.ChartData averageTemperatureSeries, averageHumiditySeries, averageLightSeries;
	private List<Double> averageTemperatureQueue, averageHumidityQueue, averageLightQueue;

	public MainFrame() {
		listener = new MqttListener("localhost", MqttListener.PORT, "java-listener");

		listener.registerListener("sensors/temperature", (m) -> {
			updateDouble(m, temperatureSeries, temperatureGraph, averageTemperatureQueue::add);
			System.out.println(averageTemperatureQueue);
		});

		listener.registerListener("sensors/humidity", (m) -> {
			updateDouble(m, humiditySeries, humidityGraph, averageHumidityQueue::add);
		});

		listener.registerListener("sensors/light", (m) -> {
			updateDouble(m, lightSeries, lightGraph, averageLightQueue::add);
		});

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(2, 2, 0, 0));

		JPanel temperaturePanel = new JPanel();
		contentPane.add(temperaturePanel);
		temperaturePanel.setLayout(new BorderLayout(0, 0));

		temperatureGraph = new JLineGraph();
		temperatureSeries = temperatureGraph.createSeries("0").setBorderColor(Color.RED).setFillColor(new Color(255, 0, 0, 128));
		temperatureGraph.overrideMaxValue(60);
		temperatureGraph.setMinorAxisStep(5);

		JLabel lblTemperature = new JLabel("Temperature");
		temperaturePanel.add(lblTemperature, BorderLayout.NORTH);
		temperaturePanel.add(temperatureGraph);

		JPanel humidityPanel = new JPanel();
		contentPane.add(humidityPanel);
		humidityPanel.setLayout(new BorderLayout(0, 0));

		humidityGraph = new JLineGraph();
		humiditySeries = humidityGraph.createSeries("0").setBorderColor(Color.BLUE).setFillColor(new Color(0, 0, 255, 128));
		humidityGraph.overrideMaxValue(100);
		humidityGraph.setMinorAxisStep(10);

		JLabel lblHumidity = new JLabel("Humidity");
		humidityPanel.add(lblHumidity, BorderLayout.NORTH);
		humidityPanel.add(humidityGraph);

		JPanel lightPanel = new JPanel();
		contentPane.add(lightPanel);
		lightPanel.setLayout(new BorderLayout(0, 0));

		lightGraph = new JLineGraph();
		lightSeries = lightGraph.createSeries("0").setBorderColor(Color.GREEN).setFillColor(new Color(0, 255, 0, 128));
		lightGraph.overrideMaxValue(100);
		lightGraph.setMinorAxisStep(10);

		JLabel lblLight = new JLabel("Brightness");
		lightPanel.add(lblLight, BorderLayout.NORTH);
		lightPanel.add(lightGraph);

		JPanel averagePanel = new JPanel();
		contentPane.add(averagePanel);
		averagePanel.setLayout(new BorderLayout(0, 0));

		averageGraph = new JLineGraph();
		averageGraph.setNextFilled(false);
		averageTemperatureSeries = averageGraph.createSeries("Temperature").setBorderColor(Color.RED).setFillColor(new Color(255, 0, 0, 128));
		averageHumiditySeries = averageGraph.createSeries("Humidity").setBorderColor(Color.BLUE).setFillColor(new Color(0, 0, 255, 128));
		averageLightSeries = averageGraph.createSeries("Brightness").setBorderColor(Color.GREEN).setFillColor(new Color(0, 255, 0, 128));
		averageGraph.overrideMaxValue(100);
		averageGraph.setMinorAxisStep(5);

		averageHumidityQueue = new ArrayList<>();
		averageLightQueue = new ArrayList<>();
		averageTemperatureQueue = new ArrayList<>();

		JLabel lblAverage = new JLabel("Averages");
		averagePanel.add(lblAverage, BorderLayout.NORTH);
		averagePanel.add(averageGraph);
		averagePanel.add(averageGraph.createLegend(true, true), BorderLayout.EAST);

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				updateDouble(averageHumidityQueue.stream().mapToDouble(Double::valueOf).average().orElse(0), averageHumiditySeries, averageGraph);
				averageHumidityQueue.clear();
				updateDouble(averageLightQueue.stream().mapToDouble(Double::valueOf).average().orElse(0), averageLightSeries, averageGraph);
				averageLightQueue.clear();
				updateDouble(averageTemperatureQueue.stream().mapToDouble(Double::valueOf).average().orElse(0), averageTemperatureSeries, averageGraph);
				averageTemperatureQueue.clear();
			}
		}, 10_000, 10_000);

	}

	private void updateDouble(MqttMessage m, ChartData series, JLineGraph graph, Consumer<Double> callback) {
		updateDouble(new String(m.getPayload()), series, graph, callback);
	}

	private void updateDouble(String m, ChartData series, JLineGraph graph, Consumer<Double> callback) {
		try {
			updateDouble(Double.valueOf(m), series, graph);
			callback.accept(Double.valueOf(m));
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
		}
	}

	private void updateDouble(double m, ChartData series, JLineGraph graph) {
		List<Double> data = series.getValues();
		data.add(m);

		if (data.size() > 300) {
			data.remove(0);
		}

		graph.repaint();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
