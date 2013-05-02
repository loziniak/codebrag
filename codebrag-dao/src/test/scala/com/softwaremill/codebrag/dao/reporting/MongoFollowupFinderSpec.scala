package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{ThreadDetails, Followup}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler

class MongoFollowupFinderSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers with MongoFollowupFinderSpecFixture {

  var followupDao: FollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _
  var followupFinder: FollowupFinder = _

  override def beforeEach() {
    FollowupRecord.drop
    CommitInfoRecord.drop
    followupDao = new MongoFollowupDAO
    followupFinder = new MongoFollowupFinder
    commitInfoDao = new MongoCommitInfoDAO
    storeAllCommits
  }

  it should "find all follow-ups only for given user" in {
    // given
    storeUserFollowups
    storeAnotherUserFollowup

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(TargetUserId)

    // then
    userFollowups.followups should have size(2)
  }

  it should "return user follow-ups with newest first order" in {
    // given
    storeUserFollowups

    // when
    val userFollowups = followupFinder.findAllFollowupsForUser(TargetUserId).followups

    // then
    userFollowups(First).date should equal(laterDate.toDate)
    userFollowups(Second).date should equal(date.toDate)
  }

  def storeUserFollowups {
    List(
      Followup(FixtureCommit1.id, TargetUserId, date, ThreadDetails(FixtureCommit1.id)),
      Followup(FixtureCommit2.id, TargetUserId, laterDate, ThreadDetails(FixtureCommit2.id)))
    .foreach(followupDao.createOrUpdateExisting(_))
  }

  def storeAnotherUserFollowup {
    followupDao.createOrUpdateExisting(Followup(FixtureCommit3.id, OtherUserId, latestDate, ThreadDetails(FixtureCommit3.id)))
  }

  def storeAllCommits {
    List(FixtureCommit1, FixtureCommit2, FixtureCommit3).foreach(commitInfoDao.storeCommit(_))
  }

}

trait MongoFollowupFinderSpecFixture {

  val TargetUserId = ObjectIdTestUtils.oid(12)
  val OtherUserId = ObjectIdTestUtils.oid(25)

  val date = DateTime.now()
  val laterDate = date.plusMinutes(1)
  val latestDate = date.plusMinutes(10)

  val First = 0
  val Second = 1

  val FixtureCommit1 = CommitInfoAssembler.randomCommit.get
  val FixtureCommit2 = CommitInfoAssembler.randomCommit.get
  val FixtureCommit3 = CommitInfoAssembler.randomCommit.get
}
