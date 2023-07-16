import React from "react";
import { LockClock } from "./LockClock";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LockClock",
  component: LockClock,
};

export const Default = () => <LockClock />;
export const Fill = () => <LockClock fill="blue" />;
export const Size = () => <LockClock height="50" width="50" />;
export const CustomStyle = () => <LockClock style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LockClock className="custom-class" />;
