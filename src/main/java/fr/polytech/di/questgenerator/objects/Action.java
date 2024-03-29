package fr.polytech.di.questgenerator.objects;

import fr.polytech.di.questgenerator.enums.ActionType;
import fr.polytech.di.questgenerator.enums.ObjectiveType;
import fr.polytech.di.questgenerator.interfaces.GameListener;
import fr.polytech.di.questgenerator.objects.xml.XMLStringObjectiveElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import static fr.polytech.di.questgenerator.enums.ActionType.*;
import static fr.polytech.di.questgenerator.enums.ObjectiveType.*;

/**
 * Define an actionType to do.
 * <p>
 * Created by COUCHOUD Thomas & COLEAU Victor.
 */
public class Action implements GameListener
{
	private final ActionType actionType;
	private final Optional<HashMap<ObjectiveType, XMLStringObjectiveElement>> objectives;
	private final Optional<Quest> subquest;
	private final boolean splittable;
	private final int depth;
	private final Quest parentQuest;
	private boolean done;

	/**
	 * Constructor.
	 *
	 * @param parentQuest The parent quest.
	 * @param actionExecutorClass The ActionExecutor that built that Action.
	 * @param depth The depth of the actionType.
	 * @param actionType The ActionType associated to this Action.
	 */
	public Action(Quest parentQuest, Class actionExecutorClass, int depth, ActionType actionType)
	{
		this(parentQuest, actionExecutorClass, depth, actionType, true);
	}

	/**
	 * Constructor.
	 *
	 * @param parentQuest The parent quest.
	 * @param actionExecutorClass The ActionExecutor that built that Action.
	 * @param depth The depth of the actionType.
	 * @param actionType The ActionType associated to this Action.
	 * @param splittable Define if this Action can be splitted.
	 */
	public Action(Quest parentQuest, Class actionExecutorClass, int depth, ActionType actionType, boolean splittable)
	{
		this(parentQuest, actionExecutorClass, depth, actionType, Optional.empty(), splittable);
	}

	/**
	 * Constructor.
	 * @param parentQuest The parent quest.
	 * @param actionExecutorClass The ActionExecutor that built that Action.
	 * @param depth The depth of the actionType.
	 * @param actionType The ActionType associated to this Action.
	 * @param objectives The objectives for the Action.
	 */
	public Action(Quest parentQuest, Class actionExecutorClass, int depth, ActionType actionType, HashMap<ObjectiveType, XMLStringObjectiveElement> objectives)
	{
		this(parentQuest, actionExecutorClass, depth, actionType, Optional.ofNullable(objectives));
	}

	/**
	 * Constructor.
	 *
	 * @param parentQuest The parent quest.
	 * @param actionExecutorClass The ActionExecutor that built that Action.
	 * @param depth The depth of the actionType.
	 * @param actionType The ActionType associated to this Action.
	 * @param objectives The objectives for the Action.
	 */
	public Action(Quest parentQuest, Class actionExecutorClass, int depth, ActionType actionType, Optional<HashMap<ObjectiveType, XMLStringObjectiveElement>> objectives)
	{
		this(parentQuest, actionExecutorClass, depth, actionType, objectives, true);
	}

	/**
	 * Constructor.
	 *
	 * @param parentQuest The parent quest.
	 * @param actionExecutorClass The ActionExecutor that built that Action.
	 * @param depth The depth of the actionType.
	 * @param actionType The ActionType associated to this Action.
	 * @param objectives The objectives for the Action.
	 * @param splittable Define if this Action can be splitted.
	 */
	public Action(Quest parentQuest, Class actionExecutorClass, int depth, ActionType actionType, HashMap<ObjectiveType, XMLStringObjectiveElement> objectives, boolean splittable)
	{
		this(parentQuest, actionExecutorClass, depth, actionType, Optional.ofNullable(objectives), splittable);
	}

	/**
	 * Constructor.
	 *
	 * @param parentQuest The parent quest.
	 * @param actionExecutorClass The ActionExecutor that built that Action.
	 * @param depth The depth of the actionType.
	 * @param actionType The ActionType associated to this Action.
	 * @param objectives The objectives for the Action.
	 * @param splittable Define if this Action can be splitted.
	 */
	public Action(Quest parentQuest, Class actionExecutorClass, int depth, ActionType actionType, Optional<HashMap<ObjectiveType, XMLStringObjectiveElement>> objectives, boolean splittable)
	{
		this.parentQuest = parentQuest;
		this.depth = depth;
		this.actionType = actionType;
		if(!objectives.isPresent())
			objectives = Optional.of(new HashMap<>());
		objectives.get().put(ObjectiveType.CLASS, new XMLStringObjectiveElement("class", actionExecutorClass.getSimpleName()));
		this.objectives = objectives;
		this.splittable = splittable;
		this.done = false;
		this.subquest = this.genSubquest(depth);
	}

	/**
	 * Generate the subquest for this actionType.
	 *
	 * @param depth The depth of the subquest.
	 * @return The quest.
	 */
	private Optional<Quest> genSubquest(int depth)
	{
		if(!this.splittable || this.actionType.isEmpty())
			return Optional.empty();
		return actionType.genSubquest(this, depth, this.objectives);
	}

	/**
	 * Returns the subquest of this actionType.
	 *
	 * @return The subquest.
	 */
	public Optional<Quest> getSubquest()
	{
		return this.subquest;
	}

	/**
	 * Used to get the formatted sentence describing this Action.
	 *
	 * @return The actionType description.
	 */
	public String getAsString()
	{
		return this.actionType.getAsString(objectives);
	}

	@Override
	public String toString()
	{
		return getAsString();
	}

	/**
	 * Get the ActionType associated to this Action.
	 *
	 * @return The ActionType of this Action.
	 */
	public ActionType getActionType()
	{
		return this.actionType;
	}

	/**
	 * Used to get the depth of this action.
	 *
	 * @return The depth.
	 */
	public int getDepth()
	{
		return this.depth;
	}

	/**
	 * Used to get the value of an objective for this action.
	 *
	 * @param objective The objective to get.
	 * @return The objective value.
	 */
	public XMLStringObjectiveElement getObjective(ObjectiveType objective)
	{
		if(!objectives.isPresent() || !objectives.get().containsKey(objective))
			return new XMLStringObjectiveElement("ERR", "ERR");
		return objectives.get().get(objective);
	}

	/**
	 * Used to know if that action is marked as done.
	 *
	 * @return True if done, false if not.
	 */
	public boolean isDone()
	{
		if(this.subquest.isPresent())
			return this.subquest.get().isDone();
		return this.done;
	}

	/**
	 * Set the value of the done state.
	 *
	 * @param done The value to set.
	 */
	public void setDone(boolean done)
	{
		this.done = done;
		notifyActionDone(this);
	}

	/**
	 * Used to know if this action is doable now.
	 *
	 * @return True if doable, false if not.
	 */
	public boolean isDoable()
	{
		return this.getParentQuest() == null || this.getParentQuest().isActionDoable(this);
	}

	/**
	 * Used to get the parent quest.
	 *
	 * @return The parent quest.
	 */
	public Quest getParentQuest()
	{
		return this.parentQuest;
	}

	/**
	 * Used to get the action we have to do, including its sub actions if present.
	 *
	 * @return The Action to do.
	 */
	public Action getActionToDo()
	{
		return this.subquest.isPresent() ? this.subquest.get().getActionToDo() : this;
	}

	/**
	 * Used to notify to the parent quest that an action has been done.
	 *
	 * @param action The action done.
	 */
	public void notifyActionDone(Action action)
	{
		this.getParentQuest().notifyActionDone(action);
	}

	/**
	 * Used to notify to the parent quest that a quest has been done.
	 *
	 * @param quest The quest done.
	 */
	public void notifyQuestDone(Quest quest)
	{
		this.getParentQuest().notifyQuestDone(quest);
	}

	/**
	 * Used to know if the objective given is the one wanted.
	 *
	 * @param objectiveType The objectivetype to test.
	 * @param objective The objective value.
	 * @return True if it's the correct one, false if not.
	 */
	private boolean isCorrectObjective(ObjectiveType objectiveType, XMLStringObjectiveElement objective)
	{
		return this.getObjective(objectiveType).is(objective);
	}

	@Override
	public boolean captureEvent(XMLStringObjectiveElement pnj)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().captureEvent(pnj);
		if(this.actionType == CAPTURE && isCorrectObjective(OBJECTIVE, pnj))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean damageEvent(XMLStringObjectiveElement target)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().damageEvent(target);
		if(this.actionType == DAMAGE && isCorrectObjective(OBJECTIVE, target))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean defendEvent(XMLStringObjectiveElement object)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().defendEvent(object);
		if(this.actionType == DEFEND && isCorrectObjective(OBJECTIVE, object))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean escortEvent(XMLStringObjectiveElement pnj)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().escortEvent(pnj);
		if(this.actionType == ESCORT && isCorrectObjective(OBJECTIVE, pnj))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean exchangeEvent(XMLStringObjectiveElement objectGive, XMLStringObjectiveElement objectGet, XMLStringObjectiveElement to)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().exchangeEvent(objectGive, objectGet, to);
		if(this.actionType == EXCHANGE && isCorrectObjective(OBJ_GIVE, objectGive) && isCorrectObjective(OBJ_GET, objectGet) && isCorrectObjective(PNJ, to))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean experimentEvent(XMLStringObjectiveElement object)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().experimentEvent(object);
		if(this.actionType == EXPERIMENT && isCorrectObjective(OBJECTIVE, object))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean exploreEvent(XMLStringObjectiveElement area)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().exploreEvent(area);
		if(this.actionType == EXPLORE && isCorrectObjective(OBJECTIVE, area))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean gatherEvent(XMLStringObjectiveElement object)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().gatherEvent(object);
		if(this.actionType == GATHER && isCorrectObjective(OBJECTIVE, object))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean getEvent(XMLStringObjectiveElement object, XMLStringObjectiveElement from)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().getEvent(object, from);
		if(this.actionType == GET && isCorrectObjective(OBJ_GET, object) && isCorrectObjective(LOC_OBJECTIVE, from))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean giveEvent(XMLStringObjectiveElement object, XMLStringObjectiveElement to)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().giveEvent(object, to);
		if(this.actionType == GIVE && isCorrectObjective(OBJ_GIVE, object) && isCorrectObjective(LOC_OBJECTIVE, to))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean gotoEvent(XMLStringObjectiveElement area)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().gotoEvent(area);
		if(this.actionType == GOTO && isCorrectObjective(OBJECTIVE, area))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean killEvent(XMLStringObjectiveElement pnj)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().killEvent(pnj);
		if(this.actionType == KILL && isCorrectObjective(OBJECTIVE, pnj))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean learnEvent(XMLStringObjectiveElement object)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().learnEvent(object);
		if(this.actionType == LEARN && isCorrectObjective(OBJECTIVE, object))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean listenEvent(XMLStringObjectiveElement pnj)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().exploreEvent(pnj);
		if(this.actionType == LISTEN && isCorrectObjective(OBJECTIVE, pnj))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean readEvent(XMLStringObjectiveElement object)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().readEvent(object);
		if(this.actionType == READ && isCorrectObjective(OBJECTIVE, object))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean repairEvent(XMLStringObjectiveElement object)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().repairEvent(object);
		if(this.actionType == REPAIR && isCorrectObjective(OBJECTIVE, object))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean reportEvent(XMLStringObjectiveElement to)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().reportEvent(to);
		if(this.actionType == REPORT && isCorrectObjective(OBJECTIVE, to))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean spyEvent(XMLStringObjectiveElement on)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().spyEvent(on);
		if(this.actionType == SPY && isCorrectObjective(OBJECTIVE, on))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean stealEvent(XMLStringObjectiveElement object, XMLStringObjectiveElement from)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().stealEvent(object, from);
		if(this.actionType == STEAL && isCorrectObjective(OBJ_GET, object) && isCorrectObjective(PNJ, from))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean stealthEvent(XMLStringObjectiveElement object)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().stealthEvent(object);
		if(this.actionType == STEALTH && isCorrectObjective(OBJECTIVE, object))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean takeEvent(XMLStringObjectiveElement object, XMLStringObjectiveElement from)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().takeEvent(object, from);
		if(this.actionType == TAKE && isCorrectObjective(OBJ_GET, object) && isCorrectObjective(PNJ, from))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean useEvent(XMLStringObjectiveElement used, XMLStringObjectiveElement on)
	{
		if(this.isDone() || !isDoable())
			return false;
		if(this.subquest.isPresent())
			return this.subquest.get().useEvent(used, on);
		if(this.actionType == USE && isCorrectObjective(OBJ_USE, used) && isCorrectObjective(LOC_OBJECTIVE, on))
		{
			setDone(true);
			return true;
		}
		return false;
	}

	/**
	 * Used to write itself into an XML file.
	 *
	 * @param out The XMLWriter.
	 * @throws XMLStreamException If the action couldn't be written.
	 */
	public void createXML(XMLStreamWriter out) throws XMLStreamException
	{
		out.writeStartElement("action");
		out.writeAttribute("type", this.getActionType().name());
		if(this.objectives.isPresent())
		{
			out.writeStartElement("objectives");
			for(ObjectiveType objectiveType : this.objectives.get().keySet())
			{
				out.writeStartElement("objective");
				out.writeAttribute("type", objectiveType.name());
				out.writeAttribute("value", this.objectives.get().get(objectiveType).getValue());
				out.writeAttribute("path", this.objectives.get().get(objectiveType).getPath());
				out.writeEndElement();
			}
			out.writeEndElement();
		}
		if(this.subquest.isPresent())
			this.subquest.get().createXML(out);
		out.writeEndElement();
	}

	/**
	 * Used to get all the used objectives in the action and its parents.
	 *
	 * @return A list of the elements used.
	 */
	public Collection<XMLStringObjectiveElement> getUsedObjectives()
	{
		ArrayList<XMLStringObjectiveElement> objectives = new ArrayList<>();
		objectives.addAll(this.getParentQuest().getUsedObjectives());
		objectives.addAll(this.getObjectivesValues());
		return objectives;
	}

	private Collection<XMLStringObjectiveElement> getObjectivesValues()
	{
		if(this.getObjectives().isPresent())
			return this.getObjectives().get().values();
		return Collections.emptyList();
	}

	/**
	 * Used to get the objectives of the action.
	 *
	 * @return The optional hashmap of the objectives.
	 */
	private Optional<HashMap<ObjectiveType, XMLStringObjectiveElement>> getObjectives()
	{
		return this.objectives;
	}
}
