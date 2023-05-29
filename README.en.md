[中文](README.md) / English

<p align="left">
    <a href="https://opensource.org/licenses/Apache-2.0" alt="License">
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" /></a>
<a target="_blank" href="https://join.slack.com/t/neatlogichome/shared_invite/zt-1w037axf8-r_i2y4pPQ1Z8FxOkAbb64w">
<img src="https://img.shields.io/badge/Slack-Neatlogic-orange" /></a>
</p>

---

## About

Neatlogic-report is a report module that supports users to configure report data sources and filtering conditions based
on their needs and scenarios. For example, using reports to summarize all work orders from the past month, the module
comes with functions such as report templates, report management, sending plans and large screen management.

## Feature

### Report Template

Report templates are composed of condition configuration, data source configuration, and content configuration. In
content configuration, the style of data source display can be defined, such as using tables, bar charts, pie charts,
etc. Report templates provide convenience for scenarios with high reuse frequency and support limiting the scope of
users through authorization.
![img.png](README_IMAGES/img.png)

### Report Management

Report management is based on referencing report templates, configuring filtering conditions according to requirements,
and supporting the control of visible personnel range through authorization.
![img.png](README_IMAGES/img1.png)
The report menu list displays all the reports that the current login can view. Click on the title to view the report
details. The report display is shown in the following figure.
![img.png](README_IMAGES/img2.png)
![img.png](README_IMAGES/img4.png)

### Sending Plan

Sending plan supports configuring timers to periodically send report content to recipients.
![img.png](README_IMAGES/img3.png)

### Large screen management

Large screen management can meet the needs of users for real-time data monitoring, decision-making, promotion, and
display scenarios. The characteristics of large screens include rich and diverse styles, high freedom of data sources,
real-time data updates, and multiple types of visualization components. The large screen editing page is divided into
three areas: canvas area, component selection area, and configuration area.
![img.png](README_IMAGES/img5.png)

- Canvas area: the core editing page in the middle. It supports moving component positions and modifying component
  layers.
  ![img.png](README_IMAGES/img6.png)
- Component selection area: a series of visual chart selections such as pie chart, line chart and column chart.
- Configuration area: When selecting a component in the canvas, the right layer presents the editing configuration of
  the component. If the component is not selected, the right layer presents the editing configuration of the component.