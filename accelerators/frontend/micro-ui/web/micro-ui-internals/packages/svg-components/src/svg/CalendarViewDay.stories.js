import React from "react";
import { CalendarViewDay } from "./CalendarViewDay";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CalendarViewDay",
  component: CalendarViewDay,
};

export const Default = () => <CalendarViewDay />;
export const Fill = () => <CalendarViewDay fill="blue" />;
export const Size = () => <CalendarViewDay height="50" width="50" />;
export const CustomStyle = () => <CalendarViewDay style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CalendarViewDay className="custom-class" />;
