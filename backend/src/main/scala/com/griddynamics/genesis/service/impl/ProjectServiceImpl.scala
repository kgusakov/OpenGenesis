package com.griddynamics.genesis.service.impl

import com.griddynamics.genesis.common.CRUDService
import com.griddynamics.genesis.api.{RequestResult, Project}
import com.griddynamics.genesis.validation.Validation
import org.springframework.transaction.annotation.Transactional
import com.griddynamics.genesis.repository.ProjectRepository
import com.griddynamics.genesis.validation.Validation._

trait ProjectService extends CRUDService[Project, Int] {
}

class ProjectServiceImpl(repository: ProjectRepository) extends ProjectService with Validation[Project] {
  protected def validateCreation(project: Project): Option[RequestResult] = {
    filterResults(Seq(
      must(project, "name must be unique") {
        project => project.id.isEmpty
      },
      mustMatchName(project.name, "name"),
      mustMatchUserName(project.projectManager, "projectManager")
    ))
  }

  protected def validateUpdate(project: Project): Option[RequestResult] = {
    filterResults(Seq(
      must(project, "name must be unique") {
        project =>
          get(project.id.get) match {
            case None => true
            case Some(prj) => prj.id == project.id
          }
      },
      mustMatchName(project.name, "name"),
      mustMatchUserName(project.projectManager, "projectManager")
    ))
  }

  @Transactional(readOnly = true)
  def get(key: Int): Option[Project] = {
    repository.get(key)
  }

  @Transactional(readOnly = true)
  def list: Seq[Project] = {
    repository.list
  }

  @Transactional
  override def create(project: Project): RequestResult = {
    validCreate(project, a => repository.save(a))
  }

  @Transactional
  override def update(project: Project): RequestResult = {
      validUpdate(project, a => {
        get(a.id.get) match {
          case None => RequestResult(isSuccess = false, compoundServiceErrors = Seq("Project '%d' is not found".format(a.id)))
          case Some(p) => {
            repository.save(a)
            RequestResult(isSuccess = true)
          }
        }
      })
  }

  @Transactional
  override def delete(project: Project): RequestResult = {
    repository.delete(project.id.get)
    RequestResult(isSuccess = true)
  }
}
