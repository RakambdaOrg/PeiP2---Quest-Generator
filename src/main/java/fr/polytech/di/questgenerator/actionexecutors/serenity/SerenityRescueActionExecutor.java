package fr.polytech.di.questgenerator.actionexecutors.serenity;

import fr.polytech.di.questgenerator.enums.Actions;
import fr.polytech.di.questgenerator.enums.Objectives;
import fr.polytech.di.questgenerator.interfaces.ActionExecutor;
import fr.polytech.di.questgenerator.objects.Action;
import fr.polytech.di.questgenerator.objects.Quest;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by COUCHOUD Thomas & COLEAU Victor.
 */
public class SerenityRescueActionExecutor implements ActionExecutor
{
	@Override
	public Quest process(int depth, Optional<HashMap<Objectives, String>> objectives)
	{
		return new Quest(depth, new Action(depth, Actions.GOTO), new Action(depth, Actions.DAMAGE), new Action(depth, Actions.ESCORT), new Action(depth, Actions.GOTO), new Action(depth, Actions.REPORT));
	}
}
