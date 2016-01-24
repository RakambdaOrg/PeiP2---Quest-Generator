package fr.polytech.di.questgenerator.actionexecutors.action;

import fr.polytech.di.questgenerator.enums.ActionType;
import fr.polytech.di.questgenerator.enums.Objectives;
import fr.polytech.di.questgenerator.interfaces.ActionExecutor;
import fr.polytech.di.questgenerator.objects.Action;
import fr.polytech.di.questgenerator.objects.Quest;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by COUCHOUD Thomas & COLEAU Victor.
 */
public class ActionReportActionExecutor implements ActionExecutor
{
	@Override
	public Quest generateQuest(int depth, Optional<HashMap<Objectives, String>> objectives)
	{
		return new Quest(new Action(depth, ActionType.GOTO), new Action(depth, ActionType.SPY, false), new Action(depth, ActionType.GOTO), new Action(depth, ActionType.REPORT, false));
	}
}