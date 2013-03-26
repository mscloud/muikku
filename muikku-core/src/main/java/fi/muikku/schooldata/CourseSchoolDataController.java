package fi.muikku.schooldata;

import java.util.List;

import fi.muikku.model.stub.courses.CourseEntity;
import fi.muikku.model.stub.users.UserEntity;
import fi.muikku.schooldata.entity.Course;

public interface CourseSchoolDataController {

  Course createCourse(CourseEntity courseEntity, String name, UserEntity creator);

  Course findCourse(CourseEntity courseEntity);
  
  List<Course> listAll();
}