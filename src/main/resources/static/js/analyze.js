function initButtons() {
    const buttons = document.querySelectorAll('.component-button')
    buttons.forEach(button => {
        button.addEventListener('click', () => {
            const component = button.getAttribute('data-component')
            showComponent(component)
        })
    })
}

function showComponent(component) {
    if (component === 'component1') {
        getPieChart('number-container', "No-Answer-Questions Percentage", "Total Questions: 1000", "http://localhost:9090/api/questions/unansweredPercentage");
    } else if (component === 'component2') {
        getPanel('number-container', "Average & Max Answers", "Total Questions: 1000", "http://localhost:9090/api/questions/answerStats", 100);
    } else if (component === 'component3') {
        getBarChart('number-container', "Distribution of Answers", "Total Questions: 1000", "http://localhost:9090/api/questions/answerCountDistribution");
    } else if (component === 'component4') {
        getPieChart('accept-container', "Accept Answer Percentage", "Total Questions: 1000", "http://localhost:9090/api/questions/acceptedPercentage");
    } else if (component === 'component5') {
        getLineChart('accept-container', "Distribution of Solve Time", "Total Questions: 1000", "http://localhost:9090/api/questions/acceptedAnswerInterval");
    } else if (component === 'component6') {
        getPieChart('accept-container', "Upvote Comparison Percentage", "Total Questions: 1000", "http://localhost:9090/api/questions/nonAcceptedHigherUpvotesPercentage");
    } else if (component === 'component7') {
        getRotateBarChart('tag-container', "Tags with 'JAVA'", "Total Questions: 1000", "http://localhost:9090/api/questions/most-upvoted-tags");
    } else if (component === 'component8') {
        getPieChart('tag-container', "Tags of more Upvote", "Total Questions: 1000", "http://localhost:9090/api/questions/most-upvoted-tag-combos");
    } else if (component === 'component9') {
        getPieChart('tag-container', "Tags of more Views", "Total Questions: 1000", "http://localhost:9090/api/questions/most-viewed-tag-combos");
    } else if (component === 'componentX') {
        getRotateBarChart('user-container', "Distribution of User Thread Count", "Total Questions: 1000", "http://localhost:9090/api/questions/user-thread-count-distribution");
    } else if (component === 'componentY') {
        getDoubleRotateBarChart('user-container', "Distribution of who Post Answers", "Distribution of who Post Comments", "http://localhost:9090/api/questions/user-answer-count-distribution", "http://localhost:9090/api/questions/user-comment-count-distribution");
    } else if (component === 'componentZ') {
        getPieChart('user-container', "Active User Count", "Total Questions: 1000", "http://localhost:9090/api/questions/most-active-users");
    }
}

async function fetchJson(url) {
    const response = await fetch(url)
    return await response.json()
}

function getPieChart(containerName, title, subtitle, url) {
    fetchJson(url).then(data => {
        echarts.dispose(document.getElementById(containerName))
        const myChart = echarts.init(document.getElementById(containerName))
        const option = {
            title: {
                text: title, left: 'center'
            }, tooltip: {
                trigger: 'item'
            }, series: [{
                name: 'Access From', type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false, itemStyle: {
                    borderRadius: 10, borderColor: '#fff', borderWidth: 2
                }, label: {
                    show: true, position: 'outside'
                }, emphasis: {
                    label: {
                        show: true, fontSize: 40, fontWeight: 'bold'
                    }
                }, data: []
            }]
        }
        for (let i = 0; i < Math.min(data["name"].length, 20); i++) {
            const name = data["name"][i]
            const value = data["value"][i].toFixed(2)
            option.series[0].data.push({value, name})
        }
        myChart.setOption(option)
    }).catch(error => {
        console.log(error)
    })
}

function getPanel(containerName, title, subtitle, url, max) {
    fetchJson(url).then(data => {
        echarts.dispose(document.getElementById(containerName))
        console.log(data)
        const myChart = echarts.init(document.getElementById(containerName))
        const gaugeData = [{
            value: data["value"][0].toFixed(2), name: 'Average Answers', title: {
                offsetCenter: ['-30%', '80%']
            }, detail: {
                offsetCenter: ['-30%', '95%']
            }
        }, {
            value: data["value"][1].toFixed(2), name: 'Max Answers', title: {
                offsetCenter: ['30%', '80%']
            }, detail: {
                offsetCenter: ['30%', '95%']
            }
        }];
        const option = {
            series: [{
                max: max, type: 'gauge', anchor: {
                    show: true, showAbove: true, size: 18, itemStyle: {
                        color: '#FAC858'
                    }
                }, pointer: {
                    icon: 'path://M2.9,0.7L2.9,0.7c1.4,0,2.6,1.2,2.6,2.6v115c0,1.4-1.2,2.6-2.6,2.6l0,0c-1.4,0-2.6-1.2-2.6-2.6V3.3C0.3,1.9,1.4,0.7,2.9,0.7z',
                    width: 8,
                    length: '80%',
                    offsetCenter: [0, '8%']
                }, progress: {
                    show: true, overlap: true, roundCap: true
                }, axisLine: {
                    roundCap: true
                }, data: gaugeData, title: {
                    fontSize: 14
                }, detail: {
                    width: 40,
                    height: 14,
                    fontSize: 14,
                    color: '#fff',
                    backgroundColor: 'inherit',
                    borderRadius: 3,
                    formatter: '{value}'
                }
            }]
        }
        myChart.setOption(option)
    })
}

function getLineChart(containerName, title, subtitle, url) {
    fetchJson(url).then(data => {
        echarts.dispose(document.getElementById(containerName))
        const myChart = echarts.init(document.getElementById(containerName))
        const option = {
            visualMap: {
                show: false, type: 'continuous', seriesIndex: 0, min: 0, max: 400
            }, title: {
                left: 'center', text: title
            }, tooltip: {
                trigger: 'axis'
            }, xAxis: [{
                data: data["name"].slice(0, 20)
            }, {
                data: data["name"].slice(0, 20), gridIndex: 1
            }], yAxis: [{}, {
                gridIndex: 1
            }], grid: [{
                bottom: '60%'
            }, {
                top: '100%'
            }], series: {
                type: 'line', showSymbol: false, data: data["value"].slice(0, 20).map(num => num.toFixed(2)),
            }
        }
        myChart.setOption(option)
    })
}

function getBarChart(containerName, title, subtitle, url) {
    fetchJson(url).then(data => {
        echarts.dispose(document.getElementById(containerName))
        const myChart = echarts.init(document.getElementById(containerName))
        const option = {
            title: {
                text: title, left: 'center'
            }, tooltip: {
                trigger: 'axis'
            }, xAxis: {
                axisLabel: {
                    interval: 'auto', rotate: 45
                }, type: 'category', data: data["name"].slice(0, 20)
            }, yAxis: {

                type: 'value'
            }, series: [{
                data: data["value"].slice(0, 20).map(num => num.toFixed(2)), type: 'bar'
            }]
        }
        myChart.setOption(option)
    })
}

async function getDoubleRotateBarChart(containerName, title1, title2, url1, url2) {
    const promises = [await fetchJson(url1), await fetchJson(url2)]
    const [data1, data2] = await Promise.all(promises);
    echarts.dispose(document.getElementById(containerName))
    const myChart = echarts.init(document.getElementById(containerName))
    console.log(data1);
    console.log(data2);
    const option = {
        title: [{
            text: title1, left: 'center'
        }, {
            text: title2, left: 'center', gridIndex: 1
        }], tooltip: [{
            trigger: 'axis'
        }, {
            trigger: 'axis', gridIndex: 1
        }], yAxis: [{
            type: 'category', data: data1["name"].slice(0, 20).reverse(), axisLabel: {
                interval: 'auto', rotate: 45
            }
        }, {
            type: 'category', data: data2["name"].slice(0, 20).reverse(), axisLabel: {
                interval: 'auto', rotate: 45, gridIndex: 1
            }
        }], xAxis: [{
            type: 'value'
        }, {
            type: 'value', gridIndex: 1
        }], series: [{
            data: data1["value"].slice(0, 20).map(num => num.toFixed(2)).reverse(),
            type: 'bar',
            xAxisIndex: 0,
            yAxisIndex: 0
        }, {
            data: data2["value"].slice(0, 20).map(num => num.toFixed(2)).reverse(),
            type: 'bar',
            gridIndex: 1,
            xAxisIndex: 1,
            yAxisIndex: 1
        }], grid: [{
            top: '10%', bottom: '50%'
        }, {
            top: '60%', bottom: '10%'
        }]
    }
    myChart.setOption(option)
}

function getRotateBarChart(containerName, title, subtitle, url) {
    fetchJson(url).then(data => {
        echarts.dispose(document.getElementById(containerName))
        const myChart = echarts.init(document.getElementById(containerName))
        const option = {
            title: {
                text: title, left: 'center'
            }, tooltip: {
                trigger: 'axis'
            }, yAxis: {
                type: 'category', data: data["name"].slice(0, 20).reverse(), axisLabel: {
                    interval: 'auto', rotate: 45
                }
            }, xAxis: {
                type: 'value'
            }, series: [{
                data: data["value"].slice(0, 20).map(num => num.toFixed(2)).reverse(), type: 'bar'
            }]
        }
        myChart.setOption(option)
    })
}