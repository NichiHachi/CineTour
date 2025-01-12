import React, { useState, useEffect, useRef } from 'react'
import './SearchBar.css'
import SearchIcon from '@mui/icons-material/Search'

const useMousePosition = (ref, dependency) => {
  useEffect(() => {
    const element = ref.current

    const handleMouseMove = (e) => {
      const rect = element.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      element.style.setProperty('--mouse-x', `${x}px`)
      element.style.setProperty('--mouse-y', `${y}px`)
    }

    if (element) {
      element.addEventListener('mousemove', handleMouseMove)
    }

    return () => {
      if (element) {
        element.removeEventListener('mousemove', handleMouseMove)
      }
    }
  }, [ref, dependency])
}

function SearchBar({ placeholder, data }) {
  const [filteredData, setFilteredData] = useState([])
  const searchRef = useRef(null)
  const resultsRef = useRef(null)

  const handleFilter = (event) => {
    const searchWord = event.target.value
    const newFilter = data.filter((value) => {
      return value.title.toLowerCase().includes(searchWord.toLowerCase())
    })

    if (searchWord === '') {
      setFilteredData([])
    } else {
      setFilteredData(newFilter)
    }
  }

  useMousePosition(searchRef, [filteredData])
  useMousePosition(resultsRef, [filteredData])

  return (
    <div className="search">
      <div className="searchInputs" ref={searchRef}>
        <div className="searchInputs-content">
          <input
            type="text"
            placeholder={placeholder}
            onChange={handleFilter}
          />
          <div className="searchIcon">
            <SearchIcon />
          </div>
        </div>
      </div>
      {filteredData.length !== 0 && (
        <div className="dataResult" ref={resultsRef}>
          <div className="dataResult-content">
            {filteredData.slice(0, 5).map((value, key) => {
              return (
                <a className="dataItem" href={value.link} key={key}>
                  <p>{value.title}</p>
                </a>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}

export default SearchBar
