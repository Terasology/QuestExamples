/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.quests.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.events.BeforeItemRemovedFromInventory;
import org.terasology.tasks.Quest;
import org.terasology.tasks.components.QuestComponent;
import org.terasology.tasks.events.BeforeQuestEvent;
import org.terasology.tasks.systems.QuestSystem;
import org.terasology.registry.In;

/**
 * Handles quest attached to book entity.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BookQuestSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(BookQuestSystem.class);

    @In
    private QuestSystem questSystem;

    /**
     * This method prevents addition of quest if quest with same shortName is active.
     *
     * @param event  Event triggered by QuestSystem due to interaction with Quest Book
     * @param entity Entity on which quest is attached
     */
    @ReceiveEvent
    public void onQuestActivated(BeforeQuestEvent event, EntityRef entity) {
        String name = event.getQuestName();
        for (Quest activeQuest : questSystem.getActiveQuests()) {
            if (name.equals(activeQuest.getShortName())) {
                event.consume();
                logger.warn("Duplicate Quest " + name + " cancelled");
                break;
            }
        }
    }

    @ReceiveEvent
    public void onBookThrown(BeforeItemRemovedFromInventory event, EntityRef entity) {
        EntityRef entityThrown = event.getItem();
        if (entityThrown.hasComponent(QuestComponent.class)) {
            QuestComponent itemQuest = entityThrown.getComponent(QuestComponent.class);
            for (Quest quest : questSystem.getActiveQuests()) {
                if (quest.getShortName().equals(itemQuest.shortName)) {
                    questSystem.removeQuest(quest, true);
                    break;
                }
            }

        }
    }
}
