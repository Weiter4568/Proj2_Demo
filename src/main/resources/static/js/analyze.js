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
    const numberContainer = document.getElementById('number-container')
    const answerContainer = document.getElementById('accept-container')
    const tagContainer = document.getElementById('tag-container')
    const userContainer = document.getElementById('user-container')
    // 根据选择的组件名称进行逻辑判断，并在组件容器中显示相应的内容
    if (component === 'component1') {
        getPieChart('number-container', "No-Answer-Questions Percentage", "Total Questions: 1000", "http://localhost:9090/api/questions/unansweredPercentage");
    } else if (component === 'component2') {
        getPanel('number-container', "Average & Max Answers", "Total Questions: 1000", "http://localhost:9090/api/questions/answerStats");
    } else if (component === 'component3') {
        getBarChart('number-container', "Distribution of Answers", "Total Questions: 1000", "http://localhost:9090/api/questions/answerCountDistribution");
    } else if (component === 'component4') {
        getPieChart('accept-container', "Accept Answer Percentage", "Total Questions: 1000", "http://localhost:9090/api/questions/acceptedPercentage");
    } else if (component === 'component5') {
        getLineChart('accept-container', "Distribution of Solve Time", "Total Questions: 1000", "http://localhost:9090/api/questions/acceptedAnswerInterval");
    } else if (component === 'component6') {
        getPieChart('accept-container', "Upvote Comparison Percentage", "Total Questions: 1000", "http://localhost:9090/api/questions/nonAcceptedHigherUpvotesPercentage");
    } else if (component === 'component7') {
        getBarChart('tag-container', "Tags with 'JAVA'", "Total Questions: 1000", "http://localhost:9090/api/questions/most-upvoted-tags");
    } else if (component === 'component8') {
        getLineChart('tag-container', "Tags for more Upvote", "Total Questions: 1000", "http://localhost:9090/api/questions/most-upvoted-tag-combos");
    } else if (component === 'component9') {
        getLineChart('tag-container', "Tags for more Views", "Total Questions: 1000", "http://localhost:9090/api/questions/most-viewed-tag-combos");
    } else if (component === 'componentX') {
        getLineChart('user-container', "Distribution of User Thread Count", "Total Questions: 1000", "http://localhost:9090/api/questions/user-thread-count-distribution");
    } else if (component === 'componentY') {
        getBarChart('user-container', "Distribution of who Post Answers", "Total Questions: 1000", "http://localhost:9090/api/questions/user-answer-count-distribution");
    } else if (component === 'componentZ') {
        getBarChart('user-container', "Distribution of who Post Comments", "Total Questions: 1000", "http://localhost:9090/api/questions/most-active-users");
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
                text: title, subtext: subtitle, left: 'center'
            }, tooltip: {
                trigger: 'item'
            }, legend: {
                top: '5%', left: 'center', top: '50px'
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
        for (let i = 0; i < data["name"].length; i++) {
            const name = data["name"][i]
            const value = data["value"][i]
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
                left: 'center', text: title, subtext: subtitle
            }, tooltip: {
                trigger: 'axis'
            }, xAxis: [{
                data: data["name"]
            }, {
                data: data["name"], gridIndex: 1
            }], yAxis: [{}, {
                gridIndex: 1
            }], grid: [{
                bottom: '60%'
            }, {
                top: '100%'
            }], series: {
                type: 'line', showSymbol: false, data: data["value"],
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
                text: title, subtext: subtitle, left: 'center'
            }, tooltip: {
                trigger: 'axis'
            }, xAxis: {
                type: 'category', data: data["name"]
            }, yAxis: {
                type: 'value'
            }, series: [{
                data: data["value"], type: 'bar'
            }]
        }
        myChart.setOption(option)
    })
}