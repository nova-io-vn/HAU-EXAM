import {useEffect,useState} from 'react'
import {questionsApi} from '../api/questionsApi'
import {normalizeCatalog} from '../model/questionModel'

export function useQuestionCatalogs(subjectId,chapterId){
  const [catalogs,setCatalogs]=useState({subjects:[],chapters:[],topics:[],error:null})
  useEffect(()=>{
    let active=true
    async function load(){
      try{
        const [subjects,chapters,topics]=await Promise.all([questionsApi.subjects(),subjectId?questionsApi.chapters(subjectId):[],chapterId?questionsApi.topics(chapterId):[]])
        if(active)setCatalogs({subjects:normalizeCatalog(subjects),chapters:normalizeCatalog(chapters),topics:normalizeCatalog(topics),error:null})
      }catch(error){if(active)setCatalogs({subjects:[],chapters:[],topics:[],error})}
    }
    load()
    return()=>{active=false}
  },[subjectId,chapterId])
  return catalogs
}
