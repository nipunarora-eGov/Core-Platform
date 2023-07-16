import React from "react";
import { DinnerDining } from "./DinnerDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DinnerDining",
  component: DinnerDining,
};

export const Default = () => <DinnerDining />;
export const Fill = () => <DinnerDining fill="blue" />;
export const Size = () => <DinnerDining height="50" width="50" />;
export const CustomStyle = () => <DinnerDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DinnerDining className="custom-class" />;
