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
        getPieChart('number-container', "Percentage of No-Answer-Questions", "Total Questions: 1000", "http://localhost:9090/test/number/a");
    } else if (component === 'component2') {
        getLineChart('number-container', "Percentage of No-Answer-Questions", "Total Questions: 1000", "http://localhost:9090/test/number/a");
    } else if (component === 'component3') {
        getBarChart('number-container', "Percentage of No-Answer-Questions", "Total Questions: 1000", "http://localhost:9090/test/number/a");
    } else if (component === 'component4') {
        answerContainer.innerHTML = 'This is Component 4.'
    } else if (component === 'component5') {
        answerContainer.innerHTML = 'This is Component 5.'
    } else if (component === 'component6') {
        answerContainer.innerHTML = 'This is Component 6.'
    } else if (component === 'component7') {
        tagContainer.innerHTML = 'This is Component 7.'
    } else if (component === 'component8') {
        tagContainer.innerHTML = 'This is Component 8.'
    } else if (component === 'component9') {
        tagContainer.innerHTML = 'This is Component 9.'
    } else if (component === 'componentX') {
        userContainer.innerHTML = 'This is Component 10.'
    } else if (component === 'componentY') {
        userContainer.innerHTML = 'This is Component 11.'
    } else if (component === 'componentZ') {
        userContainer.innerHTML = 'This is Component 12.'
    }
}

async function fetchJson(url) {
    const response = await fetch(url)
    return await response.json()
}

function getPieChart(containerName, title, subtitle, url) {
    fetchJson(url).then(data => {
        console.log(data)
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
    return true
}

function getLineChart(containerName, title, subtitle, url) {
    fetchJson(url).then(data => {
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