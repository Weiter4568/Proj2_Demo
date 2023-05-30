const API_URL = "http://10.25.228.126:8080"

let timer = null
let comb = 3

function changeComb(delta) {
    clearTimeout(timer)
    comb = Math.round(comb + delta)
    document.querySelector("#comb").textContent = comb
    timer = setTimeout(() => getTagCombStatistics({limit: 10, combSize: comb}).then(data => updateCombSvg(data)), 500)
    return false
}

function updateCombSvg(data) {
    f = d3.format(".2s")
    const {upvoteTagComb, upvoteTagCombNum, viewTagComb, viewTagCombNum} = data
    d3.select("#comb_vote").selectAll("li").data(upvoteTagComb)
        .join("li").text((d, i) => `${d} ${f(upvoteTagCombNum[i])}`)
    d3.select("#comb_view").selectAll("li").data(viewTagComb)
        .join("li").text((d, i) => `${d} ${f(viewTagCombNum[i])}`)
}


async function main() {
    const thread_summaries = await getThreadSummaries()
    const n_questions = thread_summaries.length
    const n_questions_with_answers = thread_summaries.filter(({answers}) => answers).length
    const n_questions_without_answers = n_questions - n_questions_with_answers
    visAnswerCount({n_questions, n_questions_with_answers, n_questions_without_answers})

    getTagCombStatistics({limit: 10, combSize: comb}).then(data => updateCombSvg(data))

    const answerNums = thread_summaries.map(({answers}) => answers)
    visAnsDist({answerNums})

    const accepted_info = await getAcceptInfo()
    const {acceptedAnsNum, noAcceptedAnsNum} = accepted_info
    visAcceptCount({n_accepted_questions: acceptedAnsNum, n_unsolved_questions: noAcceptedAnsNum})

    const {acceptedAnsUpvoteHigherNum, nonAcceptedAnsUpvoteHigherNum} = accepted_info
    visAnswerPri({
        n_accepted_ans_with_higher_vote: acceptedAnsUpvoteHigherNum,
        n_accepted_ans_with_lower_vote: nonAcceptedAnsUpvoteHigherNum
    })

    let resolutionTime = await getResolutionTime()
    resolutionTime = resolutionTime.filter(x => x > 0).map(x => x / 3600)
    visResolutionTime({resolutionTime})

    const tagStatistics = await getTagStatistics({limit: 100})
    const {commonTags, commonTagsNum} = tagStatistics
    visCommonTags({tags: commonTags, tagsNum: commonTagsNum})

    const {upvoteMostTagList, upvoteMostTagListNum} = tagStatistics
    visVoteTags({tags: upvoteMostTagList, tagsNum: upvoteMostTagListNum})

    const {viewMostTagList, viewMostTagListNum} = tagStatistics
    visViewTags({tags: viewMostTagList, tagsNum: viewMostTagListNum})

    const participation = thread_summaries.map(t => t.participants)
    visParticipationDist({participation})

    const userStatistics = await getUserStatistics({limit: 20})
    const questionActiveUsers = userStatistics.questionActivateUsers.filter(u => u.type === "registered").slice(0, 10)
    vizActiveQuestioners({users: questionActiveUsers})

    const answerActiveUsers = userStatistics.answerActivateUsers.filter(u => u.type === "registered").slice(0, 10)
    vizActiveAnswerers({users: answerActiveUsers})

    const activeUsers = userStatistics.participateActivateUsers.filter(u => u.type === "registered").slice(0, 10)
    visActiveUsers({users: activeUsers})
}

function visActiveUsers({users}) {
    const data = users.map(({userStackId: id, name, answerNum, commentNum}) => [{
        x: answerNum,
        y: `${name} (id:${id})`,
        z: 'answers'
    }, {x: commentNum, y: `${name} (id:${id})`, z: 'comments'},]).flat()
    StackedBarChart(data, "#act_part_viz", {
        x: d => d.x,
        y: d => d.y,
        z: d => d.z,
        xLabel: "Count →",
        yDomain: d3.groupSort(data, D => d3.sum(D, d => d.x), d => d.y), // sort y by x
        zDomain: ["answers", "comments"],
        colors: [d3.schemeCategory10[4], d3.schemeCategory10[5]],
        width: 800,
        height: 400,
        marginLeft: 250,
    })
}

function vizActiveQuestioners({users}) {
    BarChart(users, "#ques_part_viz", {
        y: d => `${d.name} (id:${d.userStackId})`,
        x: d => d.questionNum,
        xFormat: "d",
        xLabel: "Questions →",
        width: 800,
        height: 400,
        marginLeft: 250,
        color: d3.schemeCategory10[3]
    })
}

function vizActiveAnswerers({users}) {
    BarChart(users, "#ans_part_viz", {
        y: d => `${d.name} (id:${d.userStackId})`,
        x: d => d.answerNum,
        xFormat: "d",
        xLabel: "Answers →",
        width: 800,
        height: 400,
        marginLeft: 250,
        color: d3.schemeCategory10[4]
    })
}

function visParticipationDist({
                                  participation
                              }) {
    const width = document.querySelector("#part_dist").getBoundingClientRect().width, height = 300, marginX = 50,
        marginY = 20
    const svg = d3.select("#part_dist").append("svg")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr("transform", `translate(${[marginX, marginY]})`)
    const extent = d3.extent(participation)

    const x = d3.scaleLinear().domain(extent).range([0, width - marginX * 2])
    const kde = kernelDensityEstimator(kernelEpanechnikov(7), x.ticks(60))
    const density = kde(participation)

    svg.append("g")
        .attr("transform", `translate(${[0, height - marginY * 2]})`)
        .call(d3.axisBottom(x))
    const max_y = d3.max(density.map(([_, x]) => x))
    const y = d3.scaleLinear().domain([0, max_y]).range([height - marginY * 2, 0])
    const line = d3.line().curve(d3.curveBasis).x(d => x(d[0])).y(d => y(d[1]))
    svg.append("g").call(d3.axisLeft(y))

    svg.append('path').datum([[density[0][0], 0], ...density, [density[density.length - 1][0], 0]])
        .attr('fill', '#69b3a2').attr('opacity', 0.8)
        .attr('stroke', 'black').attr('stroke-width', 1)
        .attr('stroke-linejoin', 'round')
        .attr('d', line)
}

function visResolutionTime({
                               resolutionTime
                           }) {
    // resolution_viz
    const width = document.querySelector("#resolution_viz").getBoundingClientRect().width, height = 300, marginX = 50,
        marginY = 20
    const svg = d3.select("#resolution_viz").append("svg")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr("transform", `translate(${[marginX, marginY]})`)
    // const extent = d3.extent(resolutionTime)

    const x = d3.scaleLinear().domain([0, 100]).range([0, width - marginX * 2])
    const kde = kernelDensityEstimator(kernelEpanechnikov(7), x.ticks(100))
    const density = kde(resolutionTime)

    svg.append("g")
        .attr("transform", `translate(${[0, height - marginY * 2]})`)
        .call(d3.axisBottom(x))
    const max_y = d3.max(density.map(([_, x]) => x))
    const y = d3.scaleLinear().domain([0, max_y]).range([height - marginY * 2, 0])
    const line = d3.line().curve(d3.curveBasis).x(d => x(d[0])).y(d => y(d[1]))
    svg.append("g").call(d3.axisLeft(y))

    svg.append('path').datum([[density[0][0], 0], ...density, [density[density.length - 1][0], 0]])
        .attr('fill', '#69b3a2').attr('opacity', 0.8)
        .attr('stroke', 'black').attr('stroke-width', 1)
        .attr('stroke-linejoin', 'round')
        .attr('d', line)
}

function visCommonTags({tags, tagsNum}) {
    const data = tags.map((v, i) => ({
        text: v, size: tagsNum[i]
    }))

    const scaleSize = d3.scaleLog().domain(d3.extent(tagsNum)).range([0, 1])
    const color = sz => d3.interpolateReds(scaleSize(sz))

    WordCloud(data, "#common_tag_viz", {
        width: 1000, height: 400, fontScale: 6, padding: 5, color
    })
}

function visVoteTags({tags, tagsNum}) {
    const data = tags.slice(1, 12).map((v, i) => ({
        text: v, size: tagsNum[i]
    }))
    BarChart(data, "#vote_tag_viz", {
        x: d => d.size,
        y: d => d.text, // yDomain: d3.groupSort(alphabet, ([d]) => -d.frequency, d => d.letter), // sort by descending frequency
        xFormat: "s",
        xLabel: "Votes →",
        width: 900,
        height: 400,
        marginLeft: 150,
        color: "steelblue"
    })
}

function visViewTags({tags, tagsNum}) {
    const data = tags.slice(1, 11).map((v, i) => ({
        text: v, size: tagsNum[i]
    }))
    BarChart(data, "#view_tag_viz", {
        x: d => d.size,
        y: d => d.text,
        xFormat: "s",
        xLabel: "Views →",
        width: 900,
        height: 400,
        marginLeft: 150,
        color: "steelblue"
    })
}


function BarChart(data, container, {
    x = d => d, // given d in data, returns the (quantitative) x-value
    y = (d, i) => i, // given d in data, returns the (ordinal) y-value
    title, // given d in data, returns the title text
    marginTop = 30, // the top margin, in pixels
    marginRight = 0, // the right margin, in pixels
    marginBottom = 10, // the bottom margin, in pixels
    marginLeft = 30, // the left margin, in pixels
    width = 640, // the outer width of the chart, in pixels
    height, // outer height, in pixels
    xType = d3.scaleLinear, // type of x-scale
    xDomain, // [xmin, xmax]
    xRange = [marginLeft, width - marginRight], // [left, right]
    xFormat, // a format specifier string for the x-axis
    xLabel, // a label for the x-axis
    yPadding = 0.1, // amount of y-range to reserve to separate bars
    yDomain, // an array of (ordinal) y-values
    yRange, // [top, bottom]
    color = "currentColor", // bar fill color
    titleColor = "white", // title fill color when atop bar
    titleAltColor = "currentColor", // title fill color when atop background
} = {}) {
    // Compute values.
    const X = d3.map(data, x);
    const Y = d3.map(data, y);

    // Compute default domains, and unique the y-domain.
    if (xDomain === undefined) xDomain = [0, d3.max(X)];
    if (yDomain === undefined) yDomain = Y;
    yDomain = new d3.InternSet(yDomain);

    // Omit any data not present in the y-domain.
    const I = d3.range(X.length).filter(i => yDomain.has(Y[i]));

    // Compute the default height.
    if (height === undefined) height = Math.ceil((yDomain.size + yPadding) * 25) + marginTop + marginBottom;
    if (yRange === undefined) yRange = [marginTop, height - marginBottom];

    // Construct scales and axes.
    const xScale = xType(xDomain, xRange);
    const yScale = d3.scaleBand(yDomain, yRange).padding(yPadding);
    const xAxis = d3.axisTop(xScale).ticks(width / 80, xFormat);
    const yAxis = d3.axisLeft(yScale).tickSizeOuter(0);

    // Compute titles.
    if (title === undefined) {
        const formatValue = xScale.tickFormat(100, xFormat);
        title = i => `${formatValue(X[i])}`;
    } else {
        const O = d3.map(data, d => d);
        const T = title;
        title = i => T(O[i], i, data);
    }

    const svg = d3.select(container).append("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", [0, 0, width, height])
        .attr("style", "max-width: 100%; height: auto; height: intrinsic;");

    svg.append("g")
        .attr("transform", `translate(0,${marginTop})`)
        .call(xAxis)
        .call(g => g.select(".domain").remove())
        .call(g => g.selectAll(".tick line").clone()
            .attr("y2", height - marginTop - marginBottom)
            .attr("stroke-opacity", 0.1))
        .call(g => g.append("text")
            .attr("x", width - marginRight)
            .attr("y", -22)
            .attr("fill", "currentColor")
            .attr("text-anchor", "end")
            .text(xLabel));

    svg.append("g")
        .attr("fill", color)
        .selectAll("rect")
        .data(I)
        .join("rect")
        .attr("x", xScale(0))
        .attr("y", i => yScale(Y[i]))
        .attr("width", i => xScale(X[i]) - xScale(0))
        .attr("height", yScale.bandwidth());

    svg.append("g")
        .attr("fill", titleColor)
        .attr("text-anchor", "end")
        .attr("font-family", "sans-serif")
        .attr("font-size", 10)
        .selectAll("text")
        .data(I)
        .join("text")
        .attr("x", i => xScale(X[i]))
        .attr("y", i => yScale(Y[i]) + yScale.bandwidth() / 2)
        .attr("dy", "0.35em")
        .attr("dx", -4)
        .text(title)
        .call(text => text.filter(i => xScale(X[i]) - xScale(0) < 20) // short bars
            .attr("dx", +4)
            .attr("fill", titleAltColor)
            .attr("text-anchor", "start"));

    svg.append("g")
        .style('font-size', 14)
        .attr("transform", `translate(${marginLeft},0)`)
        .call(yAxis);

    return svg.node();
}

function StackedBarChart(data, container, {
    x = d => d, // given d in data, returns the (quantitative) x-value
    y = (d, i) => i, // given d in data, returns the (ordinal) y-value
    z = () => 1, // given d in data, returns the (categorical) z-value
    title, // given d in data, returns the title text
    marginTop = 30, // top margin, in pixels
    marginRight = 0, // right margin, in pixels
    marginBottom = 0, // bottom margin, in pixels
    marginLeft = 40, // left margin, in pixels
    width = 640, // outer width, in pixels
    height, // outer height, in pixels
    xType = d3.scaleLinear, // type of x-scale
    xDomain, // [xmin, xmax]
    xRange = [marginLeft, width - marginRight], // [left, right]
    yDomain, // array of y-values
    yRange, // [bottom, top]
    yPadding = 0.1, // amount of y-range to reserve to separate bars
    zDomain, // array of z-values
    offset = d3.stackOffsetDiverging, // stack offset method
    order = d3.stackOrderNone, // stack order method
    xFormat, // a format specifier string for the x-axis
    xLabel, // a label for the x-axis
    colors = d3.schemeTableau10, // array of colors
} = {}) {
    // Compute values.
    const X = d3.map(data, x);
    const Y = d3.map(data, y);
    const Z = d3.map(data, z);

    // Compute default y- and z-domains, and unique them.
    if (yDomain === undefined) yDomain = Y;
    if (zDomain === undefined) zDomain = Z;
    yDomain = new d3.InternSet(yDomain);
    zDomain = new d3.InternSet(zDomain);

    // Omit any data not present in the y- and z-domains.
    const I = d3.range(X.length).filter(i => yDomain.has(Y[i]) && zDomain.has(Z[i]));

    // If the height is not specified, derive it from the y-domain.
    if (height === undefined) height = yDomain.size * 25 + marginTop + marginBottom;
    if (yRange === undefined) yRange = [height - marginBottom, marginTop];

    // Compute a nested array of series where each series is [[x1, x2], [x1, x2],
    // [x1, x2], …] representing the x-extent of each stacked rect. In addition,
    // each tuple has an i (index) property so that we can refer back to the
    // original data point (data[i]). This code assumes that there is only one
    // data point for a given unique y- and z-value.
    const series = d3.stack()
        .keys(zDomain)
        .value(([, I], z) => X[I.get(z)])
        .order(order)
        .offset(offset)(d3.rollup(I, ([i]) => i, i => Y[i], i => Z[i]))
        .map(s => s.map(d => Object.assign(d, {i: d.data[1].get(s.key)})));

    // Compute the default x-domain. Note: diverging stacks can be negative.
    if (xDomain === undefined) xDomain = d3.extent(series.flat(2));

    // Construct scales, axes, and formats.
    const xScale = xType(xDomain, xRange);
    const yScale = d3.scaleBand(yDomain, yRange).paddingInner(yPadding);
    const color = d3.scaleOrdinal(zDomain, colors);
    const xAxis = d3.axisTop(xScale).ticks(width / 80, xFormat);
    const yAxis = d3.axisLeft(yScale).tickSizeOuter(0);

    // Compute titles.
    if (title === undefined) {
        const formatValue = xScale.tickFormat(100, xFormat);
        title = i => `${Y[i]}\n${Z[i]}\n${formatValue(X[i])}`;
    } else {
        const O = d3.map(data, d => d);
        const T = title;
        title = i => T(O[i], i, data);
    }

    const svg = d3.select(container).append("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", [0, 0, width, height])
        .attr("style", "max-width: 100%; height: auto; height: intrinsic;");

    svg.append("g")
        .attr("transform", `translate(0,${marginTop})`)
        .call(xAxis)
        .call(g => g.select(".domain").remove())
        .call(g => g.selectAll(".tick line").clone()
            .attr("y2", height - marginTop - marginBottom)
            .attr("stroke-opacity", 0.1))
        .call(g => g.append("text")
            .attr("x", width - marginRight)
            .attr("y", -22)
            .attr("fill", "currentColor")
            .attr("text-anchor", "end")
            .text(xLabel));

    const bar = svg.append("g")
        .selectAll("g")
        .data(series)
        .join("g")
        .attr("fill", ([{i}]) => color(Z[i]))
        .selectAll("rect")
        .data(d => d)
        .join("rect")
        .attr("x", ([x1, x2]) => Math.min(xScale(x1), xScale(x2)))
        .attr("y", ({i}) => yScale(Y[i]))
        .attr("width", ([x1, x2]) => Math.abs(xScale(x1) - xScale(x2)))
        .attr("height", yScale.bandwidth());

    if (title) bar.append("title")
        .text(({i}) => title(i));

    svg.append("g")
        .style('font-size', 14)
        .attr("transform", `translate(${xScale(0)},0)`)
        .call(yAxis);

    return Object.assign(svg.node(), {scales: {color}});
}

function WordCloud(data, container, {
    marginTop = 0, // top margin, in pixels
    marginRight = 0, // right margin, in pixels
    marginBottom = 0, // bottom margin, in pixels
    marginLeft = 0, // left margin, in pixels
    width = 640, // outer width, in pixels
    height = 400, // outer height, in pixels
    fontFamily = "sans-serif", // font family
    fontScale = 15, // base font size
    padding = 0, // amount of padding between the words (in pixels)
    rotate = 0, // a constant or function to rotate the words
    invalidation, // when this promise resolves, stop the simulation
    color,
} = {}) {

    const svg = d3.select(container).append("svg")
        .attr("viewBox", [0, 0, width, height])
        .attr("width", width)
        .attr("font-family", fontFamily)
        .attr("text-anchor", "middle")
        .attr("style", "max-width: 100%; height: auto; height: intrinsic;");

    const g = svg.append("g").attr("transform", `translate(${marginLeft},${marginTop})`);

    const cloud = d3.layout.cloud()
        .size([width - marginLeft - marginRight, height - marginTop - marginBottom])
        .words(data)
        .padding(padding)
        .rotate(rotate)
        .font(fontFamily)
        .fontSize(d => Math.sqrt(d.size) * fontScale)
        .on("word", ({size, x, y, rotate, text}) => {
            g.append("text")
                .attr("font-size", size)
                .attr("transform", `translate(${x},${y}) rotate(${rotate})`)
                .style("fill", color(size))
                .text(text)
        })

    cloud.start();
    invalidation && invalidation.then(() => cloud.stop());
    return svg.node();
}

function visAnswerPri({
                          n_accepted_ans_with_lower_vote, n_accepted_ans_with_higher_vote
                      }) {
    document.querySelector("#n_xacc_ques").textContent = n_accepted_ans_with_lower_vote
    const data = {
        n_accepted_ans_with_higher_vote, n_accepted_ans_with_lower_vote
    }
    const n_questions = n_accepted_ans_with_higher_vote + n_accepted_ans_with_lower_vote
    const labels = {
        n_accepted_ans_with_lower_vote: `Unaccepted answers received more upvotes ${Math.round(n_accepted_ans_with_lower_vote / n_questions * 100)}% (${n_accepted_ans_with_lower_vote})`,
        n_accepted_ans_with_higher_vote: `The rest ${Math.round(n_accepted_ans_with_higher_vote / n_questions * 100)}% (${n_accepted_ans_with_higher_vote})`
    }
    const width = document.querySelector("#pri_ans_viz").getBoundingClientRect().width
    const height = 350, margin = 20

    plotPie({
        data, labels, svgContainer: "#pri_ans_viz", width, height, margin, colorSchema: d3.schemePastel1.slice(2)
    })

}

function visAnswerCount({
                            n_questions, n_questions_with_answers, n_questions_without_answers
                        }) {
    document.querySelector("#n_questions").textContent = n_questions
    document.querySelector("#n_ques_with_ans").textContent = n_questions_with_answers
    document.querySelector("#n_ques_without_ans").textContent = n_questions_without_answers
    // ans_num_viz

    const data = {n_questions_with_answers, n_questions_without_answers}
    const labels = {
        n_questions_with_answers: `${Math.round(n_questions_with_answers / n_questions * 100)}% (${n_questions_with_answers}) questions with answers`,
        n_questions_without_answers: `${Math.round(n_questions_without_answers / n_questions * 100)}% (${n_questions_without_answers}) questions with no answers`
    }
    const width = document.querySelector("#ans_num_viz").getBoundingClientRect().width
    const height = 350, margin = 20

    plotPie({
        data, labels, svgContainer: "#ans_num_viz", width, height, margin, colorSchema: d3.schemePastel1
    })
}

function visAcceptCount({
                            n_accepted_questions, n_unsolved_questions
                        }) {
    document.querySelector("#n_acc_ques").textContent = n_accepted_questions
    document.querySelector("#n_unsolved_ques").textContent = n_unsolved_questions
    const n_questions = n_accepted_questions + n_unsolved_questions
    // ans_num_viz

    const data = {n_accepted_questions, n_unsolved_questions}
    const labels = {
        n_accepted_questions: `${Math.round(n_accepted_questions / n_questions * 100)}% (${n_accepted_questions}) questions have accepted answers`,
        n_unsolved_questions: `${Math.round(n_unsolved_questions / n_questions * 100)}% (${n_unsolved_questions}) questions have no accepted answers`
    }
    const width = document.querySelector("#acc_ans_viz").getBoundingClientRect().width
    const height = 350, margin = 20

    plotPie({
        data, labels, svgContainer: "#acc_ans_viz", width, height, margin, colorSchema: d3.schemePastel2
    })
}

function plotPie({
                     data,
                     labels,
                     svgContainer,
                     colorSchema = d3.schemeCategory10,
                     width = 500,
                     height = 350,
                     margin = 20
                 }) {
    const radius = Math.min(width, height) / 2 - margin

    const sc = d3.select(svgContainer)
    const svg = sc.append("svg")
        .attr("width", width).attr("height", height)
        .append("g").attr("transform", `translate(${width / 2}, ${height / 2})`)

    const color = d3.scaleOrdinal().domain(Object.keys(data)).range(colorSchema)
    const pie = d3.pie().value(d => d[1])
    const dataReady = pie(Object.entries(data))

    const arc = d3.arc().innerRadius(radius * 0.5).outerRadius(radius * 0.8)
    const outerArc = d3.arc().innerRadius(radius * 0.9).outerRadius(radius * 0.9)
    svg.selectAll('allSlices').data(dataReady)
        .join('path')
        .attr('d', arc).attr('fill', d => color(d.data[0]))
        .attr('stroke', 'white').style('stroke-width', '2')
    svg.selectAll('allPolylines').data(dataReady)
        .join('polyline')
        .style('stroke', 'rgb(175,175,175)').style('fill', 'none')
        .attr('stroke-width', 1)
        .attr('points', d => {
            const posA = arc.centroid(d)
            const posB = outerArc.centroid(d)
            const posC = outerArc.centroid(d)
            const midAngle = d.startAngle + (d.endAngle - d.startAngle) / 2
            posC[0] = radius * 0.95 * (midAngle < Math.PI ? 1 : -1)
            return [posA, posB, posC]
        })
    svg.selectAll('allLabels').data(dataReady)
        .join('text')
        .text(d => labels[d.data[0]])
        .attr('transform', d => {
            const pos = outerArc.centroid(d)
            const midAngle = d.startAngle + (d.endAngle - d.startAngle) / 2
            pos[0] = radius * 0.99 * (midAngle < Math.PI ? 1 : -1)
            return `translate(${pos})`
        })
        .style('text-anchor', d => {
            const midAngle = d.startAngle + (d.endAngle - d.startAngle) / 2
            return (midAngle < Math.PI) ? 'start' : 'end'
        })
}

function visAnsDist({
                        answerNums
                    }) {
    const width = document.querySelector("#ans_dist_viz").getBoundingClientRect().width, height = 300, marginX = 50,
        marginY = 20
    const svg = d3.select("#ans_dist_viz").append("svg")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr("transform", `translate(${[marginX, marginY]})`)
    const extent = d3.extent(answerNums)

    document.querySelector("#max_ans_num").textContent = extent[1]
    document.querySelector("#avg_ans_num").textContent = Math.round(d3.mean(answerNums))

    const x = d3.scaleLinear().domain(extent).range([0, width - marginX * 2])
    const kde = kernelDensityEstimator(kernelEpanechnikov(7), x.ticks(40))
    const density = kde(answerNums)

    svg.append("g")
        .attr("transform", `translate(${[0, height - marginY * 2]})`)
        .call(d3.axisBottom(x))
    const max_y = d3.max(density.map(([_, x]) => x))
    const y = d3.scaleLinear().domain([0, max_y]).range([height - marginY * 2, 0])
    const line = d3.line().curve(d3.curveBasis).x(d => x(d[0])).y(d => y(d[1]))
    svg.append("g").call(d3.axisLeft(y))

    svg.append('path').datum([[density[0][0], 0], ...density, [density[density.length - 1][0], 0]])
        .attr('fill', '#69b3a2').attr('opacity', 0.8)
        .attr('stroke', 'black').attr('stroke-width', 1)
        .attr('stroke-linejoin', 'round')
        .attr('d', line)
}

function kernelDensityEstimator(kernel, X) {
    return function (V) {
        return X.map(function (x) {
            return [x, d3.mean(V, function (v) {
                return kernel(x - v)
            })]
        })
    }
}

function kernelEpanechnikov(k) {
    return function (v) {
        return Math.abs(v /= k) <= 1 ? 0.75 * (1 - v * v) / k : 0
    }
}

async function getResolutionTime() {
    const url = `${API_URL}/thread/getAllThreadSortBySolvedTime`
    const data = await fetchJson(url)
    return data
}

async function getAcceptInfo() {
    const url = `${API_URL}/thread/getAcceptedAnsInfo`
    const data = await fetchJson(url)
    return data
}

async function getTagCombStatistics({limit, combSize}) {
    const url = `${API_URL}/thread/getTagCombTelated?combSize=${combSize}&resSize=${limit}`
    const data = await fetchJson(url)
    return data
}


async function getThreadSummaries() {
    const url = `${API_URL}/thread/getAllThread`
    const data = await fetchJson(url)
    return data
}

async function getTagStatistics({limit}) {
    const url = `${API_URL}/thread/getTagRelated?showRange=${limit}`
    const data = await fetchJson(url)
    return data
}

async function getUserStatistics({limit}) {
    const url = `${API_URL}/thread/getUserRelated?resSize=${limit}`
    const data = await fetchJson(url)
    return data
}

function fetchJson(url) {
    return fetch(url).then(res => res.json())
        .then(res => {
            const {code, data} = res
            if (code != 200) {
                throw new Error('Code is not 200')
            }
            return data
        })
}