package fr.polytech.di.questgenerator.actionexecutors.comfort;

import fr.polytech.di.questgenerator.enums.ActionType;
import fr.polytech.di.questgenerator.enums.ObjectiveType;
import fr.polytech.di.questgenerator.interfaces.ActionExecutor;
import fr.polytech.di.questgenerator.objects.Action;
import fr.polytech.di.questgenerator.objects.DataHandler;
import fr.polytech.di.questgenerator.objects.ObjectiveHelper;
import fr.polytech.di.questgenerator.objects.Quest;
import fr.polytech.di.questgenerator.objects.xml.XMLStringObjectiveElement;
import java.util.HashMap;
import java.util.Optional;
import static fr.polytech.di.questgenerator.enums.ObjectiveType.OBJECTIVE;

/**
 * Created by COUCHOUD Thomas & COLEAU Victor.
 */
public class ComfortKillActionExecutor implements ActionExecutor
{
	@Override
	public Quest generateQuest(Action parent, int depth, Optional<HashMap<ObjectiveType, XMLStringObjectiveElement>> objectives)
	{
		Quest quest = new Quest(parent);
		XMLStringObjectiveElement pnjKill = DataHandler.getRandomFromCategories(parent, "pnj/beast/pests/*");
		XMLStringObjectiveElement pnjReport = DataHandler.getRandomFromCategories(parent, "pnj/being/*");
		Action actionGotoKill = new Action(quest, this.getClass(), depth, ActionType.GOTO, buildObjective(objectives, new ObjectiveHelper(OBJECTIVE, pnjKill)));
		Action actionDamage = new Action(quest, this.getClass(), depth, ActionType.DAMAGE, buildObjective(objectives, new ObjectiveHelper(OBJECTIVE, pnjKill)), false);
		Action actionGotoReport = new Action(quest, this.getClass(), depth, ActionType.GOTO, buildObjective(objectives, new ObjectiveHelper(OBJECTIVE, OBJECTIVE, pnjReport)));
		Action actionReport = new Action(quest, this.getClass(), depth, ActionType.REPORT, buildObjective(objectives, new ObjectiveHelper(OBJECTIVE, OBJECTIVE, pnjReport)), false);
		return Quest.initQuest(quest, getSentence("Comfort_Kill", actionReport.getObjective(OBJECTIVE), pnjKill), actionGotoKill, actionDamage, actionGotoReport, actionReport);
	}
}
