/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import RecordDTO from "../../../model/RecordDTO"
import { SxProps } from "@mui/system"
import { Theme } from "@mui/material/styles"
import { Box, Button, Slider, Stack, ToggleButton, ToggleButtonGroup, Typography } from "@mui/material"
import { Filter, FilterRange } from "../../../model/Filter"
import FieldSet from "../../FieldSet"
import Metric from "../../../model/Metric"
import Modifier from "../../../model/Modifier"
import iterate from "../../../utils/iterate"
import { ReactNode } from "react"
import * as d3 from "d3-format"

interface FilterProps<MODIFIER_ID extends string, METRIC_ID extends string, SCORE> {
    metrics: Record<METRIC_ID, Metric<SCORE>>
    modifiers: Record<MODIFIER_ID, Modifier<SCORE>>
    filter: Filter<MODIFIER_ID, METRIC_ID>
    setFilter: (filter: Filter<MODIFIER_ID, METRIC_ID>) => boolean
    records: RecordDTO<SCORE>[]
}

export function FilterView<MODIFIER_ID extends string, METRIC_ID extends string, SCORE>(props: FilterProps<MODIFIER_ID, METRIC_ID, SCORE>) {
    return (
        <FieldSet title={"Filter"}>
            <Stack spacing={1}>
                <ToggleButton
                    value={"check"}
                    selected={props.filter.showOnlyFrontier ?? false}
                    onChange={() => {
                        props.setFilter({
                            ...props.filter,
                            showOnlyFrontier: !props.filter.showOnlyFrontier,
                        })
                    }}
                    sx={{ textTransform: "none" }}
                >
                    Show only frontier
                </ToggleButton>
                {iterate(props.modifiers).map(([modifierId, modifier]) => (
                    <FilterButtonGroup
                        key={modifierId}
                        filter={props.filter.modifiers && props.filter.modifiers[modifierId]}
                        setFilter={(value) =>
                            props.setFilter({
                                ...props.filter,
                                modifiers: {
                                    ...(props.filter.modifiers ?? ({} as Record<MODIFIER_ID, Modifier<SCORE>>)),
                                    [modifierId]: value,
                                },
                            })
                        }
                        label={modifier.name}
                        option1={modifier.option1}
                        option2={modifier.option2}
                    />
                ))}
                {iterate(props.metrics).map(([metricId, metric]) => (
                    <FilterSlider
                        key={metricId}
                        value={props.filter.range?.[metricId]}
                        setValue={(value) =>
                            props.setFilter({
                                ...props.filter,
                                range: {
                                    ...(props.filter.range ?? ({} as Record<METRIC_ID, Metric<SCORE>>)),
                                    [metricId]: value,
                                },
                            })
                        }
                        values={props.records.map((record) => metric.get(record.score))}
                        label={metric.name}
                    />
                ))}
                <Button size="small" variant="outlined" color="primary" disabled={isEmpty(props.filter)} onClick={() => props.setFilter({})}>
                    Reset
                </Button>
            </Stack>
        </FieldSet>
    )
}

function isEmpty(obj: Object): boolean {
    return Object.entries(obj).every(([, value]) => value === undefined || (typeof value === "object" && isEmpty(value)))
}

interface FilterButtonGroupProps {
    filter?: boolean
    setFilter: (filter: boolean | undefined) => void
    label: string
    option1: ReactNode
    option2: ReactNode
    sx?: SxProps<Theme>
}

function FilterButtonGroup(props: FilterButtonGroupProps) {
    return (
        <ToggleButtonGroup
            value={props.filter !== undefined ? [props.filter ? "on" : "off"] : ["on", "off"]}
            onChange={(_, newFilterValue: string[]) => {
                if (newFilterValue.length) {
                    props.setFilter(newFilterValue.length === 1 ? newFilterValue[0] === "on" : undefined)
                } else if (props.filter !== undefined) {
                    props.setFilter(!props.filter)
                }
            }}
            aria-label={props.label}
            sx={props.sx}
            fullWidth
            size="small"
        >
            <ToggleButton value={"on"} aria-label={`${props.label}-on`} sx={{ textTransform: "none" }}>
                {props.option1}
            </ToggleButton>
            <ToggleButton value={"off"} aria-label={`${props.label}-off`} sx={{ textTransform: "none" }}>
                {props.option2}
            </ToggleButton>
        </ToggleButtonGroup>
    )
}

interface FilterSliderProps {
    value?: FilterRange
    setValue: (filter: FilterRange | undefined) => boolean
    values: (number | undefined)[]
    label: string
}

const numberFormat = d3.format(".3~f")

function FilterSlider(props: FilterSliderProps) {
    const values = [...new Set(props.values)].map((value) => (value !== undefined && value !== null ? value : Infinity)).sort((a, b) => a - b)
    return (
        <Box
            sx={{
                width: "100%",
                paddingLeft: "1.5rem",
                paddingRight: "1.5rem",
            }}
        >
            <Typography id={`filter-slider-${props.label}`}>{props.label}</Typography>
            <Slider
                aria-labelledby={`filter-slider-${props.label}`}
                valueLabelFormat={(index) => (values[index] !== undefined ? numberFormat(values[index]) : "∞")}
                valueLabelDisplay={"auto"}
                value={[props.value?.min ? values.indexOf(props.value.min) : 0, props.value?.max ? values.indexOf(props.value.max) : values.length - 1]}
                onChange={(event, v) => {
                    const [minIndex, maxIndex] = v as number[]
                    let min: number | undefined = values[minIndex]
                    let max: number | undefined = values[maxIndex]
                    if (min !== undefined || max !== undefined) {
                        if (min === values[0]) min = undefined
                        if (max === Infinity || max === values[values.length - 1]) max = undefined
                        if (!props.setValue(min !== undefined || max !== undefined ? { min, max } : undefined)) {
                            event.preventDefault()
                        }
                    }
                }}
                step={1}
                min={0}
                max={values.length - 1}
                sx={{
                    width: "100%",
                }}
            />
        </Box>
    )
}
