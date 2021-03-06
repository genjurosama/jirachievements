package com.madgnome.jira.plugins.jirachievements.data.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.madgnome.jira.plugins.jirachievements.data.ao.Achievement;
import com.madgnome.jira.plugins.jirachievements.data.ao.Difficulty;
import com.madgnome.jira.plugins.jirachievements.data.ao.UserAchievement;
import com.madgnome.jira.plugins.jirachievements.data.ao.UserWrapper;
import com.madgnome.jira.plugins.jirachievements.data.services.IUserAchievementDaoService;
import net.java.ao.Query;

import java.util.*;

public class UserAchievementDaoService extends BaseDaoService<UserAchievement> implements IUserAchievementDaoService
{
  @Override
  protected Class<UserAchievement> getClazz()
  {
    return UserAchievement.class;
  }

  public UserAchievementDaoService(ActiveObjects ao)
  {
    super(ao);
  }

  @Override
  public void addAchievementToUser(Achievement achievement, UserWrapper userWrapper)
  {
    if (get(achievement, userWrapper) == null)
    {
      UserAchievement userAchievement = ao.create(UserAchievement.class);
      userAchievement.setUserWrapper(userWrapper);
      userAchievement.setAchievement(achievement);
      userAchievement.setCreatedOn(new Date());
      userAchievement.save();
    }
  }

  @Override
  public UserAchievement get(Achievement achievement, UserWrapper userWrapper)
  {
    return get(achievement.getID(), userWrapper.getID());
  }

  @Override
  public UserAchievement get(int achievementId, int userWrapperId)
  {
    UserAchievement[] userAchievements =
            ao.find(clazz, "ACHIEVEMENT_ID = ? AND USER_WRAPPER_ID = ?", achievementId, userWrapperId);

    return userAchievements.length > 0 ? userAchievements[0] : null;
  }

  @Override
  public Map<Difficulty, Integer> getAchievementsByLevel(UserWrapper userWrapper)
  {
    // TODO : Replace to do it in one request. Not possible for now with AO
    // TODO: Replace by request with join as soon as it is fixed in AO with postgresql
    Map<Difficulty, Integer> achievementsByLevel = new HashMap<Difficulty, Integer>(Difficulty.values().length);
    for (Difficulty difficulty : Difficulty.values())
    {
      achievementsByLevel.put(difficulty, 0);
    }

    UserAchievement[] userAchievements = ao.find(UserAchievement.class, "USER_WRAPPER_ID = ?", userWrapper.getID());
    for (UserAchievement userAchievement : userAchievements)
    {
      Achievement achievement = userAchievement.getAchievement();
      if (achievement.isActive())
      {
        final Difficulty difficulty = achievement.getDifficulty();
        Integer count = achievementsByLevel.get(difficulty);
        if (count == null)
        {
          count = 0;
        }

        achievementsByLevel.put(difficulty, ++count);
      }
    }

    return achievementsByLevel;
  }

  @Override
  public List<UserAchievement> last(int maxResult)
  {
    return Arrays.asList(ao.find(getClazz(), Query.select().order("CREATED_ON DESC").limit(maxResult)));
  }
}
