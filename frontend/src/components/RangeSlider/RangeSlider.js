import React, {useState, useRef, useEffect} from 'react'
import './RangeSlider.css'
import Glow from '../Glow/Glow'

const RangeSlider = ({
                         min = 0,
                         max = 100,
                         step = 1,
                         initialMinValue,
                         initialMaxValue,
                         onChange,
                         label = "Range"
                     }) => {
    const [minValue, setMinValue] = useState(initialMinValue || min)
    const [maxValue, setMaxValue] = useState(initialMaxValue || max)
    const minRef = useRef(null)
    const maxRef = useRef(null)
    const rangeRef = useRef(null)

    useEffect(() => {
        const updateRange = () => {
            if (rangeRef.current) {
                const percentMin = ((minValue - min) / (max - min)) * 100
                const percentMax = ((maxValue - min) / (max - min)) * 100
                rangeRef.current.style.left = `${percentMin}%`
                rangeRef.current.style.width = `${percentMax - percentMin}%`
            }
        }
        updateRange()
    }, [minValue, maxValue, min, max])



    const handleMinChange = (e) => {
        const value = Math.min(Number(e.target.value), maxValue - step)
        setMinValue(value)
        if (onChange) {
            onChange({min: value, max: maxValue})
        }
    }

    const handleMaxChange = (e) => {
        const value = Math.max(Number(e.target.value), minValue + step)
        setMaxValue(value)
        if (onChange) {
            onChange({min: minValue, max: value})
        }
    }

    return (
        <Glow className="range-slider-container">
            <div className="range-slider-content">
                <div className="range-slider-header">
                    <span className="range-slider-label">{label}</span>
                    <span className="range-slider-values">
            {minValue} - {maxValue}
          </span>
                </div>

                <div className="range-slider-wrapper">
                    <div className="range-slider-track">
                        <div className="range-slider-range" ref={rangeRef}></div>
                    </div>

                    <input
                        type="range"
                        ref={minRef}
                        min={min}
                        max={max}
                        step={step}
                        value={minValue}
                        onChange={handleMinChange}
                        className="range-slider-input range-slider-input-min"
                    />

                    <input
                        type="range"
                        ref={maxRef}
                        min={min}
                        max={max}
                        step={step}
                        value={maxValue}
                        onChange={handleMaxChange}
                        className="range-slider-input range-slider-input-max"
                    />
                </div>
            </div>
        </Glow>
    )
}

export default RangeSlider