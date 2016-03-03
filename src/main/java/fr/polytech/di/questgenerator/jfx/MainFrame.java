package fr.polytech.di.questgenerator.jfx;

import fr.polytech.di.questgenerator.QuestGenerator;
import fr.polytech.di.questgenerator.enums.Resources;
import fr.polytech.di.questgenerator.interfaces.GameListener;
import fr.polytech.di.questgenerator.interfaces.MainRefresh;
import fr.polytech.di.questgenerator.interfaces.QuestListener;
import fr.polytech.di.questgenerator.jfx.contents.QuestItem;
import fr.polytech.di.questgenerator.objects.Action;
import fr.polytech.di.questgenerator.objects.DataHandler;
import fr.polytech.di.questgenerator.objects.Quest;
import fr.polytech.di.questgenerator.objects.xml.XMLStringObjectiveElement;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Main frame of the application.
 *
 * Created by COUCHOUD Thomas & COLEAU Victor.
 */
public class MainFrame extends Application implements MainRefresh, GameListener
{
	public static final boolean DEBUG = false;
	public static final int MAX_DEPTH = 3;
	private QuestItem quest;
	private Stage stage;

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
		this.stage = primaryStage;
		Scene scene = new Scene(createContent());
		primaryStage.setTitle("Quest generator");
		primaryStage.getIcons().add(new Image(Resources.JFX.getResource("icon64.png").toString()));
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	/**
	 * Create the content of the scene.
	 *
	 * @return The scene content.
	 */
	private Parent createContent()
	{
		final QuestListener questListener = new QuestListener()
		{
			@Override
			public void actionDone(Action action)
			{
				System.out.println("Action done -> " + action);
			}

			@Override
			public void questDone(Quest quest)
			{
				System.out.println("Quest done -> " + quest);
			}
		};

		BorderPane pane = new BorderPane();

		quest = new QuestItem(this, false, QuestGenerator.createNewRandomQuest(), 0);
		quest.getQuest().addQuestListener(questListener);
		ScrollPane scroll = new ScrollPane(quest);
		scroll.setPrefSize(400, 600);
		scroll.setStyle("-fx-background: " + QuestItem.getStringColor(0) + ";");

		Button reloadButton = new Button("Reload quest");
		reloadButton.setMaxWidth(Double.MAX_VALUE);
		reloadButton.setOnMouseReleased(event -> {
			quest.modifyQuest(QuestGenerator.createNewRandomQuest());
			quest.getQuest().addQuestListener(questListener);
		});

		Button exportButton = new Button("Export");
		exportButton.setMaxWidth(Double.MAX_VALUE);
		exportButton.setOnMouseReleased(event -> {
			WritableImage image = quest.snapshot(new SnapshotParameters(), null);
			File file = new File("export" + System.currentTimeMillis() + ".png");
			try
			{
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
			}
			catch(IOException ignored)
			{
			}
		});

		final XMLStringObjectiveElement element = DataHandler.getRandomFromCategories("area/wild/*");

		Button exploreButton = new Button("Explore " + element.getValue());
		exploreButton.setMaxWidth(Double.MAX_VALUE);
		exploreButton.setOnMouseReleased(event -> MainFrame.this.exploredEvent(element));

		VBox buttons = new VBox();
		buttons.getChildren().addAll(exploreButton, reloadButton, exportButton);

		pane.setCenter(scroll);
		pane.setBottom(buttons);

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
		return result;	}

	@Override
	public boolean escortEvent(XMLStringObjectiveElement pnj)
	{
		boolean result = this.quest.getQuest().escortEvent(pnj);
		if(result)
			refresh();
		return result;	}

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
	public boolean exploredEvent(XMLStringObjectiveElement area)
	{
		boolean result = this.quest.getQuest().exploredEvent(area);
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
