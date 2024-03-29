package fr.polytech.di.questgenerator.jfx;

import fr.polytech.di.questgenerator.QuestGenerator;
import fr.polytech.di.questgenerator.enums.Resources;
import fr.polytech.di.questgenerator.interfaces.GameListener;
import fr.polytech.di.questgenerator.interfaces.MainRefresh;
import fr.polytech.di.questgenerator.jfx.contents.EventNode;
import fr.polytech.di.questgenerator.jfx.contents.QuestNode;
import fr.polytech.di.questgenerator.objects.xml.XMLStringObjectiveElement;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Main frame of the application.
 * <p>
 * Created by COUCHOUD Thomas & COLEAU Victor.
 */
public class MainFrame extends Application implements MainRefresh, GameListener
{
	private static final String PARAM_DEV = "--dev", PARAM_DEBUG = "--debug";
	private QuestNode quest;

	/**
	 * Startup function.
	 *
	 * @param args Arguments.
	 */
	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		QuestGenerator.setDebug(this.getParameters().getUnnamed().contains(PARAM_DEBUG));
		Scene scene = new Scene(createContent());
		primaryStage.setTitle("Quest generator");
		primaryStage.getIcons().add(new Image(Resources.JFX.getResource("icon64.png").toString()));
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
		refresh();
	}

	/**
	 * Create the content of the scene.
	 *
	 * @return The scene content.
	 */
	private Parent createContent()
	{
		MenuBar menuBar = new MenuBar();
		Menu menuFile = new Menu("File");
		menuBar.getMenus().addAll(menuFile);
		MenuItem reloadMenuItem = new MenuItem("Reload quest");
		reloadMenuItem.setOnAction(event -> reloadQuest());
		reloadMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
		MenuItem exportMenuItem = new MenuItem("Export");
		exportMenuItem.setOnAction(event -> export());
		exportMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
		MenuItem eventsMenuItem = new MenuItem("Events frame");
		eventsMenuItem.setOnAction(event -> {
			Stage stage1 = new Stage();
			stage1.setTitle("Events");
			stage1.getIcons().add(new Image(Resources.JFX.getResource("icon64.png").toString()));
			stage1.setScene(new Scene(createEventContent()));
			stage1.sizeToScene();
			stage1.show();
		});
		eventsMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
		MenuItem presentationMenuItem = new MenuItem("Switch presentation mode");
		presentationMenuItem.setOnAction(event -> {
			this.quest.setDoable(!this.quest.getDoable());
			this.quest.reloadQuest();
		});
		presentationMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+P"));
		MenuItem debugMenuItem = new MenuItem("Switch debug mode");
		debugMenuItem.setOnAction(event -> {
			QuestGenerator.setDebug(!QuestGenerator.getDebug());
			this.quest.reloadQuest();
		});
		debugMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));
		menuFile.getItems().addAll(reloadMenuItem, exportMenuItem, eventsMenuItem, presentationMenuItem, debugMenuItem);
		quest = new QuestNode(this, !this.getParameters().getUnnamed().contains(PARAM_DEV), QuestGenerator.createNewRandomQuest(), 0);
		ScrollPane scroll = new ScrollPane(quest);
		scroll.setMaxHeight(Double.MAX_VALUE);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setStyle("-fx-background: " + QuestNode.getStringColor(0) + ";");
		scroll.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
			quest.setMaxWidth(scroll.getViewportBounds().getWidth());
		});
		Slider depthSlider = new Slider();
		depthSlider.setMin(1);
		depthSlider.setMax(20);
		depthSlider.setValue(QuestGenerator.getMaxDepth());
		depthSlider.setShowTickLabels(true);
		depthSlider.setShowTickMarks(true);
		depthSlider.setMajorTickUnit(1);
		depthSlider.setBlockIncrement(1);
		depthSlider.setMinorTickCount(1);
		depthSlider.valueProperty().addListener((ov, old_val, new_val) -> {
			QuestGenerator.setMaxDepth(new_val.intValue());
			depthSlider.adjustValue(QuestGenerator.getMaxDepth());
		});
		depthSlider.setMaxWidth(Double.MAX_VALUE);
		Button reloadButton = new Button("Reload quest");
		reloadButton.setMaxWidth(Double.MAX_VALUE);
		reloadButton.setOnMouseReleased(event -> reloadQuest());
		VBox buttons = new VBox();
		buttons.setPadding(new Insets(2, 2, 2, 2));
		buttons.getChildren().addAll(depthSlider, reloadButton);
		VBox root = new VBox();
		root.setStyle("-fx-background: " + QuestNode.getStringColor(0) + ";");
		root.setPrefSize(800, 600);
		root.getChildren().addAll(menuBar, buttons, scroll);
		VBox.setVgrow(scroll, Priority.ALWAYS);
		return root;
	}

	/**
	 * Used to export the current displayed quest.
	 */
	private void export()
	{
		ChoiceDialog<String> dialog = new ChoiceDialog<>(null, "PNG", "XML", "RAW", "ACTIONS");
		dialog.setTitle("Exporting...");
		dialog.setHeaderText("Choose the format you want to export in");
		dialog.setContentText("Format:");
		Optional<String> value = dialog.showAndWait();
		if(!value.isPresent())
			return;
		File file;
		switch(value.get())
		{
			case "PNG":
				WritableImage image = quest.snapshot(new SnapshotParameters(), null);
				file = new File("export" + System.currentTimeMillis() + ".png");
				try
				{
					ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
				}
				catch(IOException ignored)
				{
				}
				break;
			case "RAW":
				file = new File("export" + System.currentTimeMillis() + ".txt");
				try(PrintWriter fis = new PrintWriter(new FileOutputStream(file)))
				{
					for(String line : this.quest.getQuest().getAsString(true, "[{0}]"))
						fis.println(line);
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				break;
			case "XML":
				file = new File("export" + System.currentTimeMillis() + ".xml");
				XMLStreamWriter out = null;
				try
				{
					out = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
					out.writeStartDocument();
					this.quest.getQuest().createXML(out);
					out.writeEndDocument();
				}
				catch(XMLStreamException | FileNotFoundException | UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
				finally
				{
					if(out != null)
						try
						{
							out.close();
						}
						catch(XMLStreamException e)
						{
						}
				}
				break;
			case "ACTIONS":
				file = new File("export" + System.currentTimeMillis() + ".txt");
				try(PrintWriter fis = new PrintWriter(new FileOutputStream(file)))
				{
					for(String line : this.quest.getQuest().getActionString(true))
						fis.println(line);
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				break;
		}
	}

	/**
	 * Used to display a new random quest.
	 */
	private void reloadQuest()
	{
		quest.modifyQuest(QuestGenerator.createNewRandomQuest());
	}

	/**
	 * Used to create the event frame content.
	 *
	 * @return The parent element.
	 */
	private Parent createEventContent()
	{
		VBox pane = new VBox(5);
		pane.setPadding(new Insets(2, 2, 2, 2));
		List<Method> methods = Arrays.asList(GameListener.class.getMethods());
		methods.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
		for(Method method : methods)
			pane.getChildren().add(new EventNode(this, method));
		pane.setPrefWidth(250);
		return pane;
	}

	@Override
	public void refresh()
	{
		this.quest.refresh();
	}

	@Override
	public boolean captureEvent(XMLStringObjectiveElement pnj)
	{
		boolean result = this.quest.getQuest().captureEvent(pnj);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean damageEvent(XMLStringObjectiveElement target)
	{
		boolean result = this.quest.getQuest().damageEvent(target);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean defendEvent(XMLStringObjectiveElement object)
	{
		boolean result = this.quest.getQuest().defendEvent(object);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean escortEvent(XMLStringObjectiveElement pnj)
	{
		boolean result = this.quest.getQuest().escortEvent(pnj);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean exchangeEvent(XMLStringObjectiveElement objectGive, XMLStringObjectiveElement objectGet, XMLStringObjectiveElement to)
	{
		boolean result = this.quest.getQuest().exchangeEvent(objectGive, objectGet, to);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean experimentEvent(XMLStringObjectiveElement object)
	{
		boolean result = this.quest.getQuest().experimentEvent(object);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean exploreEvent(XMLStringObjectiveElement area)
	{
		boolean result = this.quest.getQuest().exploreEvent(area);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean gatherEvent(XMLStringObjectiveElement object)
	{
		boolean result = this.quest.getQuest().gatherEvent(object);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean getEvent(XMLStringObjectiveElement object, XMLStringObjectiveElement from)
	{
		boolean result = this.quest.getQuest().getEvent(object, from);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean giveEvent(XMLStringObjectiveElement object, XMLStringObjectiveElement to)
	{
		boolean result = this.quest.getQuest().giveEvent(object, to);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean gotoEvent(XMLStringObjectiveElement area)
	{
		boolean result = this.quest.getQuest().gotoEvent(area);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean killEvent(XMLStringObjectiveElement pnj)
	{
		boolean result = this.quest.getQuest().killEvent(pnj);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean learnEvent(XMLStringObjectiveElement object)
	{
		boolean result = this.quest.getQuest().learnEvent(object);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean listenEvent(XMLStringObjectiveElement pnj)
	{
		boolean result = this.quest.getQuest().listenEvent(pnj);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean readEvent(XMLStringObjectiveElement object)
	{
		boolean result = this.quest.getQuest().readEvent(object);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean repairEvent(XMLStringObjectiveElement object)
	{
		boolean result = this.quest.getQuest().repairEvent(object);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean reportEvent(XMLStringObjectiveElement to)
	{
		boolean result = this.quest.getQuest().reportEvent(to);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean spyEvent(XMLStringObjectiveElement on)
	{
		boolean result = this.quest.getQuest().spyEvent(on);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean stealEvent(XMLStringObjectiveElement object, XMLStringObjectiveElement from)
	{
		boolean result = this.quest.getQuest().stealEvent(object, from);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean stealthEvent(XMLStringObjectiveElement object)
	{
		boolean result = this.quest.getQuest().stealthEvent(object);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean takeEvent(XMLStringObjectiveElement object, XMLStringObjectiveElement from)
	{
		boolean result = this.quest.getQuest().takeEvent(object, from);
		if(result)
			refresh();
		return result;
	}

	@Override
	public boolean useEvent(XMLStringObjectiveElement used, XMLStringObjectiveElement on)
	{
		boolean result = this.quest.getQuest().useEvent(used, on);
		if(result)
			refresh();
		return result;
	}
}
